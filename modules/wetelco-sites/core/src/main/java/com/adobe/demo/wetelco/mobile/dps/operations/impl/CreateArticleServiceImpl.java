package com.adobe.demo.wetelco.mobile.dps.operations.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.mobile.dps.DPSCollection;
import com.adobe.cq.mobile.dps.DPSEntity;
import com.adobe.cq.mobile.dps.DPSException;
import com.adobe.cq.mobile.dps.DPSProject;
import com.adobe.cq.mobile.dps.ui.PublishDataSource;
import com.adobe.demo.wetelco.mobile.dps.AEMMobileDPSCatalogStyle;
import com.adobe.demo.wetelco.mobile.dps.AEMMobileDPSPage;
import com.adobe.demo.wetelco.mobile.dps.Constants;
import com.adobe.demo.wetelco.mobile.dps.DPSCatalogProject;
import com.adobe.demo.wetelco.mobile.dps.eventing.DeleteDPSEntitiesJobConsumer;
import com.adobe.demo.wetelco.mobile.dps.eventing.PublishDPSEntitiesJobConsumer;
import com.adobe.demo.wetelco.mobile.dps.operations.CreateArticleService;
import com.adobe.demo.wetelco.mobile.dps.operations.RenditionsService;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Component(name = "Create AEM Mobile DPS Article", metatype = true, immediate = true)
@Property(name = "service.description", value = "AEM Mobile DPS Create Article Service")
@Service
public class CreateArticleServiceImpl implements CreateArticleService {
	private static final Logger log = LoggerFactory
			.getLogger(CreateArticleServiceImpl.class);

	public static final String COLLECTION_TEMPLATE = "/libs/mobileapps/dps/templates/collection/default";

	public static final String THUMBNAIL_430_430_PLACEHOLDER = "/content/dam/wetelco-sites/logo/wetelco_logo.png";
	public static final String DPS_IMAGE_LOGO = "/content/dam/weTelco/weTelco/WeTelco_BG.png";
	public static final String DPS_BG_IMAGE = "/content/dam/weTelco/weTelco/transparent.png";
	
	
	
	public static final String ARTICLES_FOLDER = "/articles";
	public static final String COLLECTIONS_FOLDER = "/collections";

	/**
	 * PARAM: If true create remote dps article if not found fail otherwise
	 */
	public static final String PARAM_CREATE_IF_MISSING = "createIfMissing";

	/**
	 * PARAM: If true upload content in addition to metadata, skip content
	 * otherwise
	 */
	public static final String PARAM_INCLUDE_CONTENT = "includeContent";
	/**
	 * PARAM: If true delete local (AEM) entity
	 */
	public static final String PARAM_DELETE_LOCAL = "deleteLocal";
	private static final String PARAM_CONTENTS = "contents";
	private static final String PARAM_APPEND_CONTENTS = "appendContents";

	/**
	 * PARAM: Target collection to add article to after creation
	 */
	private static final String PARAM_TARGET_COLLECTION = "targetCollection";

	public static final String PARAM_OPERATION = ":operation";

	public static final String OPERATION_PREFIX = "dpsapps:";
	public static final String OPERATION_UPLOAD = OPERATION_PREFIX
			+ "dpsUpload";
	public static final String OPERATION_PUBLISH = OPERATION_PREFIX
			+ "dpsPublish";
	public static final String OPERATION_UPDATE_CONTENTS = OPERATION_PREFIX
			+ "updateContents";
	public static final String OPERATION_UNPUBLISH = OPERATION_PREFIX
			+ "dpsUnpublish";
	public static final String OPERATION_DELETE = OPERATION_PREFIX
			+ "dpsDelete";

	@Reference
	ResourceResolverFactory resourceResolverFactory;

	@Reference
	RenditionsService renditionsService;

	@Reference
	AdapterManager adapterManager;

	@Reference
	private JobManager jobManager;

	@Property(label = "DIO Armory Path", description = "Path to the DIO Armoury", value = "/etc/commerce/products/wetelco")
	private static final String PROPERTY_DIO_ARMOURY_PATH = "dioArmouryPath";
	public String dioArmouryPath = null;

	@Property(label = "AEM Mobile DPS Project Path", description = "Path to the default AEM Mobile DPS Project", value = "/content/mobileapps/wetelco-aem-mobile-app")
	private static final String PROPERTY_DPS_PROJECT_PATH = "dpsProjectPath";
	private String dpsProjectPath = null;

	@Property(label = "AEM Mobile DPS Article Template", description = "Path to the default AEM Mobile DPS Article Template", value = "/content/catalogs/wetelco/en/aem-mobile-template-pages/article-page-template")
	private static final String PROPERTY_DPS_ARTICLE_TEMPLATE_PATH = "dpsArticleTemplatePath";
	private String dpsArticleTemplatePath = null;

	@Property(label = "AEM Mobile DPS Catalogs Path", description = "Path to the folder with all catalogs", value = "/content/dam/weTelco/weTelco")
	private static final String PROPERTY_UA_CATALOGS_PATH = "uaCatalogsPath";
	private String uaCatalogsPath = null;

	@Property(label = "Server base url", description = "Base part of the server url", value = "http://localhost:4502")
	private static final String PROPERTY_SERVER_BASE_URL = "serverBaseUrl";
	private String serverBaseUrl = null;

	@Property(label = "Server user", description = "User used to perform actions", value = "admin")
	private static final String PROPERTY_SERVER_USER = "serverUser";
	private String serverUser = null;

	@Property(label = "Server password", description = "Passord for given user", passwordValue = "admin")
	private static final String PROPERTY_SERVER_USER_PASSWORD = "serverUserPassword";
	private String serverUserPassword = null;

	@Property(label = "Auto upload", description = "Upload new articles to DPS", boolValue = true)
	private static final String PROPERTY_AUTO_UPLOAD_ENABLED = "autoUpload";
	private Boolean autoUploadEnabled = null;

	@Property(label = "Auto publish", description = "Publish new articles in DPS", boolValue = true)
	private static final String PROPERTY_AUTO_PUBLISH_ENABLED = "autoPublish";
	private Boolean autoPublishEnabled = null;

	private HttpClient httpclient;

	private ResourceResolver getResourceResolver(final Session session) {
		Map<String, Object> authInfo = new HashMap<String, Object>();
		authInfo.put(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session);

		try {
			return resourceResolverFactory.getResourceResolver(authInfo);
		} catch (LoginException e) {
			throw new InternalError("Unexpected LoginException");
		}
	}

	protected void activate(ComponentContext ctx) {
		dioArmouryPath = PropertiesUtil.toString(
				ctx.getProperties().get(PROPERTY_DIO_ARMOURY_PATH), null);
		dpsProjectPath = PropertiesUtil.toString(
				ctx.getProperties().get(PROPERTY_DPS_PROJECT_PATH), null);
		uaCatalogsPath = PropertiesUtil.toString(
				ctx.getProperties().get(PROPERTY_UA_CATALOGS_PATH), null);
		serverBaseUrl = PropertiesUtil.toString(
				ctx.getProperties().get(PROPERTY_SERVER_BASE_URL), null);
		serverUser = PropertiesUtil.toString(
				ctx.getProperties().get(PROPERTY_SERVER_USER), null);
		serverUserPassword = PropertiesUtil.toString(
				ctx.getProperties().get(PROPERTY_SERVER_USER_PASSWORD), null);
		autoUploadEnabled = PropertiesUtil.toBoolean(
				ctx.getProperties().get(PROPERTY_AUTO_UPLOAD_ENABLED), false);
		autoPublishEnabled = PropertiesUtil.toBoolean(
				ctx.getProperties().get(PROPERTY_AUTO_PUBLISH_ENABLED), false);
		dpsArticleTemplatePath = PropertiesUtil.toString(ctx.getProperties()
				.get(PROPERTY_DPS_ARTICLE_TEMPLATE_PATH), null);

		httpclient = new HttpClient(new MultiThreadedHttpConnectionManager());
	}

	@Override
	public String getDioArmouryPath() {
		return dioArmouryPath;
	}

	@Override
	public String getDPSCatalogsPath() {
		return uaCatalogsPath;
	}

	@Override
	public String getDPSProjectPath() {
		return dpsProjectPath;
	}

	@Override
	public Boolean getIsAutoUploadEnabled() {
		return autoUploadEnabled;
	}

	@Override
	public Boolean getIsAutoPublishEnabled() {
		return autoPublishEnabled;
	}

	private Node createFolderIfNotExists(String folderPath, Session session)
			throws Exception {
		if (StringUtils.isEmpty(folderPath)) {
			return null;
		}

		if (session.nodeExists(folderPath)) {
			return session.getNode(folderPath);
		}

		return JcrUtil.createPath(folderPath,
				JcrResourceConstants.NT_SLING_FOLDER, session);
	}

	@Override
	public void updateArticle(Page articlePage, Node articleNode)
			throws Exception {
		ResourceResolver resolver = articlePage.getContentResource()
				.getResourceResolver();

		String thumbnailRef = getArticleThumbnailPath(resolver, articleNode);

		if (StringUtils.isEmpty(thumbnailRef)
				|| (resolver.getResource(thumbnailRef) == null)) {
			thumbnailRef = THUMBNAIL_430_430_PLACEHOLDER;
			log.info("ARTICLE THUMBNAIL DOES NOT EXIST, SETTING PLACEHOLDER FOR - "
					+ articleNode.getPath());
		}

		ModifiableValueMap articleMap = articlePage.getContentResource()
				.adaptTo(ModifiableValueMap.class);
		articleMap.put("dioStyle", articleNode.getPath());
		Resource imageResource = articlePage.getContentResource().getChild(
				"image");

		if (imageResource != null) {
			Node imageNode = imageResource.adaptTo(Node.class);

			if (imageNode != null) {
				imageNode.setProperty("fileReference", thumbnailRef);
			}
		}
	}

	@Override
	public Page getArticle(Resource catalogResource) {
		ResourceResolver resolver = catalogResource.getResourceResolver();
		PageManager pageManager = resolver.adaptTo(PageManager.class);
		AEMMobileDPSCatalogStyle catalogStyle = catalogResource
				.adaptTo(AEMMobileDPSCatalogStyle.class);
		final String articleFolder = dpsProjectPath + ARTICLES_FOLDER;
		final String name = catalogStyle.getName();
		final String path = articleFolder + "/" + name;
		return pageManager.getPage(path);
	}

	@Override
	public Page createArticle(Session session, Node articleNode)
			throws Exception {
		Page newArticle = null;

		try {
			if ((session == null) || (articleNode == null)) {
				return null;
			}

			ResourceResolver resolver = getResourceResolver(session);
			PageManager pageManager = resolver.adaptTo(PageManager.class);

			Resource dpsProject = resolver.getResource(dpsProjectPath);
			DPSCatalogProject dpsCatalogProject = dpsProject
					.adaptTo(DPSCatalogProject.class);

			String articleTemplate = dpsArticleTemplatePath;
			// dpsCatalogProject.getArticleTemplate();

			String articleFolder = dpsProjectPath + ARTICLES_FOLDER;
			Resource catalogResource = resolver.getResource(articleNode
					.getPath());
			AEMMobileDPSCatalogStyle catalogStyle = catalogResource
					.adaptTo(AEMMobileDPSCatalogStyle.class);
			final String name = catalogStyle.getName();
			final String title = catalogStyle.getTitle();
			String path = articleFolder + "/" + name;

			if (session.nodeExists(path)) {
				log.info("Article Exists - " + path);
				return pageManager.getPage(path);
			}

			createFolderIfNotExists(articleFolder, session);

			newArticle = pageManager.create(articleFolder, name,
					articleTemplate, title);

			updateArticle(newArticle, articleNode);

			log.info("Created Article - " + newArticle.getPath());
		} catch (Exception e) {
			log.error("Error creating article", e);
		}

		return newArticle;
	}

	@Override
	public Page createArticle(Page productPage) throws Exception {
		Page newArticle = null;

		try {
			// 1. First get the node location for WeTelco AEM Mobile
			Node articleNode = productPage.adaptTo(Node.class);
			Session session = articleNode.getSession();

			if ((session == null) || (articleNode == null)) {
				return null;
			}

			ResourceResolver resolver = getResourceResolver(session);
			PageManager pageManager = resolver.adaptTo(PageManager.class);

			/*
			 * String articleTemplate = dpsArticleTemplatePath; Resource
			 * articleTemlateResource = resolver.getResource(articleTemplate);
			 * Node articleTemlateResourceNode =
			 * articleTemlateResource.adaptTo(Node.class);
			 */
			// dpsCatalogProject.getArticleTemplate();

			String articleFolder = dpsProjectPath + ARTICLES_FOLDER;
			final String name = productPage.getName();
			final String title = productPage.getTitle();
			String path = articleFolder + "/" + name;

			if (session.nodeExists(path)) {
				log.info("Article Exists - " + path);
				return pageManager.getPage(path);
			}

			createFolderIfNotExists(articleFolder, session);

			Resource articleFolderResource = resolver
					.getResource(articleFolder);
			Node articleFolderResourceNode = articleFolderResource
					.adaptTo(Node.class);
			Node newlyCreatedArticleNode = JcrUtil.copy(articleNode,
					articleFolderResourceNode, name);

			Node newlyCreatedArticleNodeProduct = newlyCreatedArticleNode
					.getNode("jcr:content/content-par/ng-product");
			newlyCreatedArticleNodeProduct.setProperty("sling:resourceType",
					"weTelco/weTelco/components/article");
			Node newlyCreatedArticleNodeProductImge = newlyCreatedArticleNode
					.getNode("jcr:content/content-par/ng-product/image");

			Node newlyCreatedImageNodeRenamedForArticle = JcrUtil.copy(
					newlyCreatedArticleNodeProductImge,
					newlyCreatedArticleNodeProduct, "article-image");
			newlyCreatedImageNodeRenamedForArticle.setProperty(
					"sling:resourceType", "foundation/components/image");
			newlyCreatedArticleNodeProductImge.remove();

			// Now set some key properties
			Node newlyCreatedArticleNodeJcrContent = newlyCreatedArticleNode
					.getNode("jcr:content");
			newlyCreatedArticleNodeJcrContent.setProperty(
					"dps-smoothScrolling", "always");
			newlyCreatedArticleNodeJcrContent.setProperty("dps-title", title);
			newlyCreatedArticleNodeJcrContent.setProperty("dps-access",
					"metered");
			newlyCreatedArticleNodeJcrContent.setProperty("dps-resourceType",
					"dps:Article");
			newlyCreatedArticleNodeJcrContent.setProperty("pge-type",
					"app-content");
			String []internalKeywords = new String[1];
			internalKeywords[0] = "launch";
			newlyCreatedArticleNodeJcrContent.setProperty("dps-internalKeywords", internalKeywords);
			
			// Change the template and resource type to make sure we use DPS
			// article tempalte and resource
			newlyCreatedArticleNodeJcrContent.setProperty("sling:resourceType",
					"weTelco/weTelco/components/pages/article");
			newlyCreatedArticleNodeJcrContent.setProperty("cq:template",
					"/apps/weTelco/weTelco/templates/article-page");
			
			
			// Make sure you can link to a collection
			/***********/
			Page categoryPage = productPage.getParent();
			Node categoryPageNode = categoryPage.adaptTo(Node.class);

			if (categoryPageNode != null && categoryPageNode.hasNode("jcr:content")) {
				Node categoryPageJcrNode = categoryPageNode.getNode("jcr:content");

				javax.jcr.Property commerceTypeProperty = categoryPageJcrNode
						.getProperty("cq:commerceType");
				// String valueOfCommerceType =
				// commerceTypeProperty.getString();

				if (commerceTypeProperty != null
						&& StringUtils.isNotBlank(commerceTypeProperty
								.getString())
						&& commerceTypeProperty.getString().equals("section")) {
					final String categoryPageTitle = categoryPage.getName();
					// final String categoryPageName = categoryPage.getName();

					newlyCreatedArticleNodeJcrContent.setProperty(
							"collectionTitle", categoryPageTitle);
					newlyCreatedArticleNodeJcrContent.setProperty(
							"collectionCat", categoryPageTitle);

				}
			}

			/**********/
			// newlyCreatedArticleNodeJcrContent.addNode("image");
			// Node imageChildNode = JcrUtils.getOrAddNode(
			// newlyCreatedArticleNodeJcrContent, "image",
			// "nt:unstructured");
			// imageChildNode.setProperty("fileReference", "");
			// imageChildNode.setProperty("sling:resourceType",
			// "foundation/components/image");
			Node newlyCreatedImageNodeForArticle = JcrUtil.copy(
					newlyCreatedImageNodeRenamedForArticle,
					newlyCreatedArticleNodeJcrContent, "image");
			newlyCreatedImageNodeForArticle.setProperty("sling:resourceType",
					"foundation/components/image");

			// Now get the path and adapt to Page
			String newArticlePagePath = newlyCreatedArticleNode.getPath();
			Resource newArticlePageResource = resolver
					.getResource(newArticlePagePath);
			newArticle = newArticlePageResource.adaptTo(Page.class);

			// newArticle = pageManager.create(articleFolder, name,
			// articleTemplate, title);
			// newArticle = copiedNode.getSession().

			// updateArticle(newArticle, articleNode);

			log.info("Created Article - " + newArticle.getPath());
		} catch (Exception e) {
			log.error("Error creating article", e);
		}

		return newArticle;
	}

	@Override
	public String getArticleThumbnailPath(Resource catalogResource)
			throws Exception {
		ResourceResolver resolver = catalogResource.getResourceResolver();
		return getArticleThumbnailPath(resolver,
				catalogResource.adaptTo(Node.class));
	}

	private String getArticleThumbnailPath(ResourceResolver resolver,
			Node articleNode) throws Exception {
		String thumbPath = "";

		if (articleNode == null) {
			return thumbPath;
		}

		Resource resource = resolver.getResource(articleNode.getPath());

		Resource cwNode = null;
		String imageUrl = null;
		Resource imgResource = null;
		ValueMap properties = null;
		Rendition rendition = null;
		Asset asset = null;

		Iterator<Resource> cwNodes = resource.listChildren();

		while (cwNodes.hasNext()) {
			cwNode = cwNodes.next();
			properties = cwNode.getValueMap();

			imageUrl = properties.get(Constants.PROP_DIO_IMAGE_URL, "");

			if (StringUtils.isEmpty(imageUrl)) {
				continue;
			}

			imgResource = resolver.getResource(imageUrl);

			if (imgResource == null) {
				continue;
			}

			asset = DamUtil.resolveToAsset(imgResource);

			if (asset == null) {
				continue;
			}

			rendition = asset.getRendition(Constants.REND_430_430);

			if (rendition == null) {
				log.trace(
						"getArticleThumbnailPath: Generate missing rendition for asset {}",
						asset.getPath());
				// try to generate rendition if it wasn't generated yet
				asset = renditionsService.generateJpegRenditions(imgResource,
						false);
				rendition = asset.getRendition(Constants.REND_430_430);
			}

			if (rendition != null) {
				thumbPath = rendition.getPath();
				// don't break, get the last available colorway 430 X 430
			}
		}

		return thumbPath;
	}

	@Override
	public Page getCollectionPageIfExists(Session session, Node collectionNode)
			throws Exception {
		Page collectionPage = null;

		try {
			if ((session == null) || (collectionNode == null)) {
				return null;
			}

			ResourceResolver resolver = getResourceResolver(session);
			PageManager pageManager = resolver.adaptTo(PageManager.class);
			Resource catalogResource = resolver.getResource(collectionNode
					.getPath());
			AEMMobileDPSCatalogStyle catalogStyle = catalogResource
					.adaptTo(AEMMobileDPSCatalogStyle.class);

			String collectionFolder = dpsProjectPath + COLLECTIONS_FOLDER;
			final String name = catalogStyle.getName();
			String path = collectionFolder + "/" + name;

			if (session.nodeExists(path)) {
				collectionPage = pageManager.getPage(path);
			}
		} catch (Exception e) {
			log.error("Error while checking collection", e);
		}

		return collectionPage;
	}

	public Page getCollectionPageIfExists(Session session, Node collectionNode,
			Page sectionPage) throws Exception {
		Page collectionPage = null;

		try {
			if ((session == null) || (collectionNode == null)) {
				return null;
			}

			ResourceResolver resolver = getResourceResolver(session);
			PageManager pageManager = resolver.adaptTo(PageManager.class);

			String collectionFolder = dpsProjectPath + COLLECTIONS_FOLDER;
			final String name = sectionPage.getName();
			String path = collectionFolder + "/" + name;

			if (session.nodeExists(path)) {
				collectionPage = pageManager.getPage(path);
			}
		} catch (Exception e) {
			log.error("Error while checking collection", e);
		}

		return collectionPage;
	}

	private HashMap<String, String> getBannerByName(DPSProject dpsProject,
			String bannerName) {
		HashMap<String, String> ret = new HashMap<String, String>();
		JSONObject banners;
		try {
			PublishDataSource publishDataSource = adapterManager.getAdapter(
					dpsProject, PublishDataSource.class);
			if (publishDataSource == null) {
				log.warn("getLayoutByName: Couldn't adapt dps project to PublishDataSource");
				return null;
			}
			banners = publishDataSource.getBanners(new HashMap());
			if (!banners.has("data")) {
				log.warn("Empty banners list");
				return ret;
			}

			JSONArray bannersArr = banners.getJSONArray("data");

			for (int i = 0; i < bannersArr.length(); i++) {
				final JSONObject banner = bannersArr.getJSONObject(i);
				final String dpsBannerName = banner.getString("title");

				if (StringUtils.isNotEmpty(dpsBannerName)
						&& dpsBannerName.equals(bannerName)) {
					ret.put("name", dpsBannerName);
					ret.put("url", banner.getString("id"));
					ret.put("title", banner.getString("title"));
					return ret;
				}
			}

		} catch (DPSException e) {
			log.warn("Couldn't get banner. banner name: " + bannerName, e);
		} catch (JSONException e) {
			log.warn("Couldn't get banner. banner name: " + bannerName, e);
		}

		return null;
	}

	@Override
	public HashMap<String, String> getLayoutByName(DPSProject dpsProject,
			String layoutName) {
		HashMap<String, String> ret = new HashMap<String, String>();
		JSONObject layouts;
		try {
			PublishDataSource publishDataSource = adapterManager.getAdapter(
					dpsProject, PublishDataSource.class);
			if (publishDataSource == null) {
				log.warn("getLayoutByName: Couldn't adapt dps project to PublishDataSource");
				return null;
			}
			layouts = publishDataSource.getLayouts(new HashMap());
			if (!layouts.has("data")) {
				log.warn("Empty layouts list");
				return ret;
			}
			JSONArray layoutsArr = layouts.getJSONArray("data");

			for (int i = 0; i < layoutsArr.length(); i++) {
				final JSONObject layout = layoutsArr.getJSONObject(i);
				final String dpsLayoutName = layout.getString("title");

				if (StringUtils.isNotEmpty(dpsLayoutName)
						&& dpsLayoutName.equals(layoutName)) {
					ret.put("name", dpsLayoutName);
					ret.put("url", layout.getString("entityURL"));
					ret.put("title", layout.getString("title"));
					return ret;
				}
			}

		} catch (DPSException e) {
			log.warn("Couldn't get layout. layoutName: " + layoutName, e);
		} catch (JSONException e) {
			log.warn("Couldn't get layout. layoutName: " + layoutName, e);
		}

		return null;
	}

	@Override
	public Page createCollection(Session session, Node collectionNode)
			throws Exception {
		Page newCollection = null;

		try {
			if ((session == null) || (collectionNode == null)) {
				return null;
			}

			ResourceResolver resolver = getResourceResolver(session);
			Resource catalogResource = resolver.getResource(collectionNode
					.getPath());
			PageManager pageManager = resolver.adaptTo(PageManager.class);

			String collectionFolder = dpsProjectPath + COLLECTIONS_FOLDER;

			newCollection = getCollectionPageIfExists(session, collectionNode);

			if (newCollection != null) {
				return newCollection;
			}

			createFolderIfNotExists(collectionFolder, session);

			// Get the title and name to use for the collection
			AEMMobileDPSCatalogStyle catalogStyle = catalogResource
					.adaptTo(AEMMobileDPSCatalogStyle.class);
			final String title = catalogStyle.getTitle();
			final String name = catalogStyle.getName();

			newCollection = pageManager.create(collectionFolder, name,
					COLLECTION_TEMPLATE, title);
			final Resource collectionContentResource = newCollection
					.getContentResource();
			ModifiableValueMap collMap = collectionContentResource
					.adaptTo(ModifiableValueMap.class);

			collMap.put("dioStyle", collectionNode.getPath());

		} catch (Exception e) {
			log.error("Error creating collection", e);
		}

		return newCollection;
	}

	@Override
	public Page createCollection(Page sectionPage) throws Exception {
		Page newCollection = null;

		try {
			// 1. First get the node location for WeTelco AEM Mobile
			Node collectionNode = sectionPage.adaptTo(Node.class);
			Session session = collectionNode.getSession();

			if ((session == null) || (collectionNode == null)) {
				return null;
			}

			ResourceResolver resolver = getResourceResolver(session);
			PageManager pageManager = resolver.adaptTo(PageManager.class);

			String collectionFolder = dpsProjectPath + COLLECTIONS_FOLDER;

			newCollection = getCollectionPageIfExists(session, collectionNode,
					sectionPage);

			if (newCollection != null) {
				return newCollection;
			}

			createFolderIfNotExists(collectionFolder, session);

			// Get the title and name to use for the collection
			final String title = sectionPage.getTitle();
			final String name = sectionPage.getName();

			newCollection = pageManager.create(collectionFolder, name,
					COLLECTION_TEMPLATE, title);
			// TODO : Set a product ID
			Node newCollectionNode = newCollection.adaptTo(Node.class);
			Node newCollectionNodeJcrContent = newCollectionNode
					.getNode("jcr:content");
			newCollectionNodeJcrContent.setProperty("dps-productId", "com.adobe.demo.wetelco." + 
					sectionPage.getName().replace("-", ""));
			newCollectionNodeJcrContent
					.setProperty(
							"dps-layout",
							"/publication/e0bb1e8d-4c5d-4278-aa55-b4468cf341aa/layout/d30f8678-d10e-c86c-e214-4026d61b20a5;version=1455595593036");
			newCollectionNodeJcrContent.setProperty("dps-layoutTitle", "4-Col");
			newCollectionNodeJcrContent.setProperty("pge-type", "app-content");

			newCollectionNodeJcrContent.setProperty("dps-readingPosition", "retain");
			newCollectionNodeJcrContent.setProperty("dps-horizontalSwipe", new Boolean(true));
			newCollectionNodeJcrContent.setProperty("dps-openDefault", "browsePage");
			newCollectionNodeJcrContent.setProperty("dps-allDownload", "on");
			String []internalKeywords = new String[1];
			internalKeywords[0] = "launch";
			newCollectionNodeJcrContent.setProperty("dps-internalKeywords", internalKeywords);
			newCollectionNodeJcrContent.setProperty("dps-abstract", name);
			newCollectionNodeJcrContent.setProperty("dps-shortTitle", name);
			newCollectionNodeJcrContent.setProperty("dps-title", name);
			newCollectionNodeJcrContent.setProperty("dps-allDownload", "on");
			newCollectionNodeJcrContent.setProperty("dps-importance", "normal");
		        
			// TODO : Create and add a image node
			Node imageChildNode = JcrUtils.getOrAddNode(
					newCollectionNodeJcrContent, "image", "nt:unstructured");
			imageChildNode.setProperty("fileReference",
					DPS_IMAGE_LOGO);
			imageChildNode.setProperty("sling:resourceType",
					"foundation/components/image");

			// TODO : Create and add a background image
			Node backgroundImageChildNode = JcrUtils.getOrAddNode(
					newCollectionNodeJcrContent, "background-image",
					"nt:unstructured");
			backgroundImageChildNode.setProperty("fileReference", DPS_BG_IMAGE);
			backgroundImageChildNode.setProperty("sling:resourceType",
					"foundation/components/image");

			// Now we need to try and associate to collection parent
			Page sectionPageParent = sectionPage.getParent();
			Node sectionPageParentNode = sectionPageParent.adaptTo(Node.class);

			if (sectionPageParentNode != null && sectionPageParentNode.hasNode("jcr:content")) {
				Node categoryPageJcrNode = sectionPageParentNode.getNode("jcr:content");

				javax.jcr.Property commerceTypeProperty = categoryPageJcrNode
						.getProperty("cq:commerceType");
				// String valueOfCommerceType =
				// commerceTypeProperty.getString();

				if (commerceTypeProperty != null
						&& StringUtils.isNotBlank(commerceTypeProperty
								.getString())
						&& commerceTypeProperty.getString().equals("section")) {
					final String categoryPageTitle = sectionPageParent.getName();
					// final String categoryPageName = categoryPage.getName();

					newCollectionNodeJcrContent.setProperty(
							"collectionTitle", categoryPageTitle);
					newCollectionNodeJcrContent.setProperty(
							"collectionCat", categoryPageTitle);

				}
			}
		} catch (Exception e) {
			log.error("Error creating collection", e);
		}

		return newCollection;
	}

	@Override
	public void upload(Page dpsPage, boolean addToParentCollection) {
		AEMMobileDPSPage uadpsPage = null;
		AEMMobileDPSPage parentCollectionPage = null;
		AEMMobileDPSCatalogStyle catalogStyle;
		AEMMobileDPSCatalogStyle parentCollectionStyle;
		boolean shouldAddBanner = false;

		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new NameValuePair(PARAM_OPERATION, OPERATION_UPLOAD));
		list.add(new NameValuePair(PARAM_CREATE_IF_MISSING, "true"));
		list.add(new NameValuePair(PARAM_INCLUDE_CONTENT, "true"));

		if (addToParentCollection) {
			uadpsPage = dpsPage.adaptTo(AEMMobileDPSPage.class);
			if (uadpsPage != null) {
				catalogStyle = uadpsPage.getCatalogStyle();
				if (catalogStyle != null) {

					if (!uadpsPage.isUploaded() && catalogStyle.isCollection()) {
						// add banner when uploading collection for the first
						// time
						shouldAddBanner = true;
					}

					parentCollectionStyle = catalogStyle.getParent();
					if (parentCollectionStyle != null) {
						// TODO make sure parent collection already exists
						// session = collectionNode.getSession();
						// Page collectionPage = createCollection(session,
						// collectionNode);
						parentCollectionPage = parentCollectionStyle
								.getDPSPage();
						if (!parentCollectionPage.isUploaded()) {
							parentCollectionPage.upload(true);
						}
						// do not add articles/collection to parent here -
						// We need to update collections contents after upload
						// either way.
						// We need to update parent collections contents because
						// we need to fix entities order.
						// log.debug("Adding article/collection to parent collection: "
						// + PARAM_TARGET_COLLECTION + "=" +
						// parentCollectionStyle.getName());
						// list.add(new NameValuePair(PARAM_TARGET_COLLECTION,
						// parentCollectionStyle.getName()));

					} else if (catalogStyle.isTopLevelResource()) {
						// if there is no parent collection we want to
						log.debug("Adding article/collection to top level collection: "
								+ PARAM_TARGET_COLLECTION
								+ "="
								+ Constants.DPS_TOP_LEVEL_COLLECTION_NAME);
						list.add(new NameValuePair(PARAM_TARGET_COLLECTION,
								Constants.DPS_TOP_LEVEL_COLLECTION_NAME));
					}
				} else {
					log.warn("Catalog style not set. current page: "
							+ dpsPage.getPath());
				}
			} else {
				log.warn("Couldn't adapt page to UADPSPage class. current page: "
						+ dpsPage.getPath());
			}
		}

		performDPSOperation(dpsPage.getPath(), OPERATION_UPLOAD,
				list.toArray(new NameValuePair[list.size()]));

		if (shouldAddBanner) {
			addBanner(dpsPage);
		}

		if (parentCollectionPage != null) {
			updateCollectionContents(parentCollectionPage);
		} else if (uadpsPage != null && uadpsPage.isCollection()) {
			// updateCollectionContents(uadpsPage);
		}

	}

	@Override
	public void addToParentCollection(Page dpsPage) {
		AEMMobileDPSPage uadpsPage = dpsPage.adaptTo(AEMMobileDPSPage.class);
		if (uadpsPage == null) {
			log.warn("Couldn't adapt page to UADPSPage class. current page: "
					+ dpsPage.getPath());
			return;
		}

		if (!uadpsPage.isUploaded()) {
			log.warn("Can't add to parent collection - need to upload first. current page: "
					+ dpsPage.getPath()
					+ ", "
					+ uadpsPage.getDpsId()
					+ ", "
					+ uadpsPage.isUploaded()
					+ ", "
					+ uadpsPage.getCatalogPath());
			return;
		}

		AEMMobileDPSCatalogStyle catalogStyle = uadpsPage.getCatalogStyle();
		if (catalogStyle == null) {
			log.warn("Catalog style not set. current page: "
					+ dpsPage.getPath());
			return;
		}

		AEMMobileDPSCatalogStyle parentCatalogStyle = catalogStyle.getParent();
		if (parentCatalogStyle == null) {
			log.warn("Couldn't get parent catalog style. current page: "
					+ dpsPage.getPath());
			return;
		}

		AEMMobileDPSPage collectionPage = parentCatalogStyle.getDPSPage();
		if (collectionPage == null) {
			log.warn("Couldn't get parent catalog page. current page: "
					+ dpsPage.getPath());
			return;
		}

		if (!collectionPage.isUploaded()) {
			log.warn("Can't add to parent collection - need to upload parent first. current page: "
					+ dpsPage.getPath());
			return;
		}

		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new NameValuePair(PARAM_OPERATION, OPERATION_UPDATE_CONTENTS));
		list.add(new NameValuePair(PARAM_CREATE_IF_MISSING, "true"));
		list.add(new NameValuePair(PARAM_INCLUDE_CONTENT, "false"));
		list.add(new NameValuePair(PARAM_APPEND_CONTENTS, "true"));
		list.add(new NameValuePair(PARAM_CONTENTS, uadpsPage.getDpsId()));

		performDPSOperation(collectionPage.getPath(),
				OPERATION_UPDATE_CONTENTS,
				list.toArray(new NameValuePair[list.size()]));
	}

	public void updateCollectionContents(AEMMobileDPSPage collectionPage) {
		List<String> contents = new ArrayList<String>();

		if (!collectionPage.isCollection()) {
			// make sure we won't handle anything other than collection
			return;
		}

		AEMMobileDPSCatalogStyle catalogStyle = collectionPage
				.getCatalogStyle();
		if (catalogStyle == null) {
			log.warn("Catalog style not set. current page: "
					+ collectionPage.getPath());
			return;
		}

		String bannerName = catalogStyle.getBanner();
		if (StringUtils.isNotEmpty(bannerName)) {
			DPSProject dpsProject = collectionPage.getDPSProject();
			HashMap<String, String> banner = getBannerByName(dpsProject,
					bannerName);
			if (banner != null) {
				contents.add(banner.get("url"));
			}
		}

		for (Resource childResource : catalogStyle.listChildren()) {
			AEMMobileDPSCatalogStyle childStyle = childResource
					.adaptTo(AEMMobileDPSCatalogStyle.class);
			// we only need to handle proper catalog nodes
			// there might be nodes like jcr:content, Thumbnail etc.
			if (childStyle != null) {
				AEMMobileDPSPage childPage = childStyle.getDPSPage();
				if (childPage != null) {
					String dpsId = childPage.getDpsId();
					if (StringUtils.isNotEmpty(dpsId)) {
						contents.add(dpsId);
					}
				}
			}
		}

		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new NameValuePair(PARAM_OPERATION, OPERATION_UPDATE_CONTENTS));
		list.add(new NameValuePair(PARAM_APPEND_CONTENTS, "false"));
		list.add(new NameValuePair(PARAM_CONTENTS, StringUtils.join(contents,
				",")));
		log.debug("Update contents of " + collectionPage.getPath()
				+ ". Set it to " + StringUtils.join(contents, ","));
		performDPSOperation(collectionPage.getPath(),
				OPERATION_UPDATE_CONTENTS,
				list.toArray(new NameValuePair[list.size()]));

		if (autoPublishEnabled) {
			collectionPage.publish(false);
		}
	}

	private void addBanner(Page collectionPage, String bannerUri) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new NameValuePair(PARAM_OPERATION, OPERATION_UPDATE_CONTENTS));
		list.add(new NameValuePair(PARAM_CONTENTS, bannerUri));
		list.add(new NameValuePair(PARAM_APPEND_CONTENTS, "true"));

		performDPSOperation(collectionPage.getPath(),
				OPERATION_UPDATE_CONTENTS,
				list.toArray(new NameValuePair[list.size()]));
	}

	public void addBanner(Page collectionPage) {
		AEMMobileDPSPage uadpsPage = collectionPage
				.adaptTo(AEMMobileDPSPage.class);
		if (uadpsPage == null) {
			log.warn("Couldn't adapt page to UADPSPage class. current page: "
					+ collectionPage.getPath());
			return;
		}
		AEMMobileDPSCatalogStyle catalogStyle = uadpsPage.getCatalogStyle();
		if (catalogStyle == null) {
			log.warn("Catalog style not set. current page: "
					+ collectionPage.getPath());
			return;
		}
		String bannerName = catalogStyle.getBanner();

		if (StringUtils.isNotEmpty(bannerName)) {
			DPSEntity dpsCollection = collectionPage
					.adaptTo(DPSCollection.class);
			DPSProject dpsProject = dpsCollection.getProject();
			HashMap<String, String> banner = getBannerByName(dpsProject,
					bannerName);
			if (banner != null) {
				addBanner(collectionPage, banner.get("url"));
			}
		}
	}

	@Override
	public void publish(Page dpsPage) {
		publish(dpsPage, true);
	}

	@Override
	public void publish(Page dpsPage, boolean publishParent) {
		Map<String, Object> payload = new HashMap<String, Object>();

		payload.put(PublishDPSEntitiesJobConsumer.PAGE_PATH, dpsPage.getPath());
		payload.put(PublishDPSEntitiesJobConsumer.PUBLISH_PARENT, publishParent);

		log.debug("Adding Publish job with - " + payload);
		jobManager.addJob(PublishDPSEntitiesJobConsumer.JOB_TOPIC, payload);
	}

	public boolean dpsPublish(Page dpsPage, boolean publishParent) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new NameValuePair(PARAM_OPERATION, OPERATION_PUBLISH));

		boolean isSuccessful = performDPSOperation(dpsPage.getPath(),
				OPERATION_PUBLISH, list.toArray(new NameValuePair[list.size()]));

		if (isSuccessful && publishParent) {
			AEMMobileDPSPage parentCollectionPage = getParentCollection(dpsPage);

			if (parentCollectionPage == null) {
				log.warn("publish: Couldn't get parent catalog page. current page: "
						+ dpsPage.getPath());
				return false;
			}

			parentCollectionPage.publish(false);
		}

		return isSuccessful;
	}

	public boolean isEntityPublished(Page dpsPage) {
		return getEntityStatus(dpsPage) == Constants.DPS_PUBLISH_STATUS.PUBLISHED;
	}

	private Constants.DPS_PUBLISH_STATUS getEntityStatus(Page dpsPage) {
		ValueMap vm = dpsPage.getContentResource().adaptTo(ValueMap.class);

		String dpsId = vm.get(Constants.PROP_DPS_ID, "");

		if (StringUtils.isEmpty(dpsId)) {
			return Constants.DPS_PUBLISH_STATUS.NEW;
		}

		Constants.DPS_PUBLISH_STATUS status = Constants.DPS_PUBLISH_STATUS.UNPUBLISHED;

		String pageDPSStatusPath = dpsPage.getPath() + ".getStatus.json";

		log.debug("Checking DPS status of - " + pageDPSStatusPath);

		String response = performGetOperation(pageDPSStatusPath);

		try {
			JSONObject jsonObject = new JSONObject(response);

			JSONObject data = jsonObject.getJSONObject("data");

			if (!data.has(dpsId)) {
				log.debug("Status is " + status + " for " + pageDPSStatusPath);
				return status;
			}

			JSONObject publication = data.getJSONObject(dpsId);

			if (!publication.getJSONObject("status").has("PUBLISHING")) {
				log.debug("Status is " + status + " for " + pageDPSStatusPath);
				return status;
			}

			JSONObject publishing = (JSONObject) publication.getJSONObject(
					"status").get("PUBLISHING");

			if (publishing.getString("type").equals("PUBLISHING")
					&& publishing.getString("status").equals("success")) {
				Date dpsLastUploaded = vm.get(Constants.PROP_DPS_LAST_UPLOADED,
						Date.class);
				String lastStatus = publishing.getString("lastStatus");

				if (StringUtils.isNotEmpty(lastStatus)
						&& (dpsLastUploaded != null)) {
					Date lastPublishedDate = Constants.DPS_DATE_FORMAT
							.parse(lastStatus);

					if (dpsLastUploaded.getTime() < lastPublishedDate.getTime()) {
						status = Constants.DPS_PUBLISH_STATUS.PUBLISHED;
					} else {
						status = Constants.DPS_PUBLISH_STATUS.PUBLISHED_OUT_OF_DATE;
					}
				} else {
					status = Constants.DPS_PUBLISH_STATUS.PUBLISHED;
				}
			}
		} catch (Exception e) {
			log.warn("Error parsing status response", e);
		}

		log.debug("Status is " + status + " for " + pageDPSStatusPath);
		return status;
	}

	private String performGetOperation(String path) {
		GetMethod method = null;
		String response = null;

		try {
			method = new GetMethod(serverBaseUrl + path);

			Credentials credentials = new UsernamePasswordCredentials(
					serverUser, serverUserPassword);

			httpclient.getState().setCredentials(AuthScope.ANY, credentials);
			httpclient.getParams().setAuthenticationPreemptive(true);

			method.setDoAuthentication(true);

			final int returnCode = httpclient.executeMethod(method);

			if (returnCode == HttpStatus.SC_OK) {
				response = method.getResponseBodyAsString();
			}
		} catch (Exception e) {
			log.error("Error while performing DPS GET of" + path, e);
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}

		return response;
	}

	@Override
	public void unpublish(Page dpsPage) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new NameValuePair(PARAM_OPERATION, OPERATION_UNPUBLISH));
		performDPSOperation(dpsPage.getPath(), OPERATION_UNPUBLISH,
				list.toArray(new NameValuePair[list.size()]));

		AEMMobileDPSPage parentCollectionPage = getParentCollection(dpsPage);
		if (parentCollectionPage == null) {
			log.warn("unpublish: Couldn't get parent catalog page. current page: "
					+ dpsPage.getPath());
			return;
		}
		parentCollectionPage.publish(false);
	}

	@Override
	public void delete(Page dpsPage) {
		Map<String, Object> payload = new HashMap<String, Object>();

		payload.put(PublishDPSEntitiesJobConsumer.PAGE_PATH, dpsPage.getPath());

		log.debug("Adding Delete job with - " + payload);
		jobManager.addJob(DeleteDPSEntitiesJobConsumer.JOB_TOPIC, payload);
	}

	@Override
	public void dpsDelete(Page dpsPage) {
		AEMMobileDPSPage uadpsPage = dpsPage.adaptTo(AEMMobileDPSPage.class);
		if (uadpsPage == null) {
			log.warn("Couldn't adapt page to UADPSPage class. current page: "
					+ dpsPage.getPath());
			return;
		}

		NameValuePair[] params = new NameValuePair[] {
				new NameValuePair(PARAM_OPERATION, OPERATION_DELETE),
				new NameValuePair(PARAM_DELETE_LOCAL, "true") };
		if (performDPSOperation(dpsPage.getPath(), OPERATION_DELETE, params)) {
			if (autoPublishEnabled) {
				AEMMobileDPSPage parentCollectionPage = uadpsPage.getParent();
				if (parentCollectionPage == null) {
					log.warn("unPublish: Can't get parent catalog page. current page: "
							+ dpsPage.getPath());
					return;
				}
				parentCollectionPage.publish(false);
			}
		} else {
			// in case removing entity failed try to unpublish it again
			// fixes situation when we lost lastPublished date
			uadpsPage.unpublish();
		}
	}

	private AEMMobileDPSPage getParentCollection(Page dpsPage) {
		AEMMobileDPSPage uadpsPage = dpsPage.adaptTo(AEMMobileDPSPage.class);
		if (uadpsPage == null) {
			return null;
		}

		return uadpsPage.getParent();
	}

	private boolean performDPSOperation(String path, String operation,
			NameValuePair[] params) {
		PostMethod method = null;
		boolean ret = true;

		try {
			method = new PostMethod(serverBaseUrl + path);
			if (log.isDebugEnabled()) {
				String paramsStr = "";
				for (NameValuePair param : params) {
					paramsStr += param.getName() + "=" + param.getValue()
							+ ", ";
				}
				log.debug("performDPSOperation - path: " + path
						+ ", http params: " + paramsStr);
			}

			method.setRequestBody(params);

			Credentials credentials = new UsernamePasswordCredentials(
					serverUser, serverUserPassword);
			httpclient.getState().setCredentials(AuthScope.ANY, credentials);
			httpclient.getParams().setAuthenticationPreemptive(true);
			method.setDoAuthentication(true);

			log.trace("DPS operation " + operation + ": Started");
			log.debug("DPS operation: " + operation + ", url: " + serverBaseUrl
					+ path);
			final int returnCode = httpclient.executeMethod(method);

			if (returnCode == HttpStatus.SC_OK) {
				log.trace("DPS operation " + operation + ", path: " + path
						+ ": [success]");
			} else {
				log.trace("DPS operation " + operation + ", path: " + path
						+ ", code: " + returnCode + ": [error]");
				ret = false;
			}
		} catch (Exception e) {
			log.error("Error while performing DPS operation. Operation: "
					+ operation, e);
			ret = false;
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
		return ret;
	}
}
