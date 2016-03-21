/**
 * 
 */
package com.adobe.demo.wetelco.mobile.dps.services;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.servlet.ServletException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.mobile.dps.DPSEntity;
import com.adobe.cq.mobile.dps.DPSException;
import com.adobe.cq.mobile.dps.DPSProject;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.AEMMobileClient;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.RequestException;
import com.adobe.demo.wetelco.mobile.dps.utils.AEMMoDUtil;
import com.adobe.demo.wetelco.mobile.dps.utils.ContentCreationUtil;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * @author vvenkata
 *
 */

@Component(name = "AEMMoDServiceImpl Service", metatype = true, immediate = true)
@Property(name = "service.description", value = "AEM Mobile DPS Create Article Service")
@Service
public class AEMMoDServiceImpl implements AEMMoDService {

	private static final Logger log = LoggerFactory
			.getLogger(AEMMoDServiceImpl.class);

	public static final String COLLECTION_TEMPLATE = "/libs/mobileapps/dps/templates/collection/default";
	public static final String PN_TARGET_COLLECTION = "collectionCat";
	public static final String THUMBNAIL_430_430_PLACEHOLDER = "/content/dam/wetelco-sites/logo/wetelco_logo.png";
	public static final String DPS_BG_IMAGE = "/content/dam/weTelco/weTelco/WeTelco_BG.png";
	public static final String DPS_IMAGE = "/content/dam/weTelco/weTelco/transparent.png";

	public static final String ARTICLES_FOLDER = "/articles";
	public static final String COLLECTIONS_FOLDER = "/collections";

	@Reference
	ResourceResolverFactory resourceResolverFactory;

	@Reference
	private JobManager jobManager;

	@Property(label = "AEM Mobile DPS Project Path", description = "Path to the default AEM Mobile DPS Project", value = "/content/mobileapps/wetelco-aem-mobile-app")
	private static final String PROPERTY_DPS_PROJECT_PATH = "dpsProjectPath";
	private String dpsProjectPath = null;

	@Reference
	private SlingRequestProcessor slingRequestProcessor;

	@Reference
	AdapterManager adapterManager;

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
		dpsProjectPath = PropertiesUtil.toString(
				ctx.getProperties().get(PROPERTY_DPS_PROJECT_PATH), null);
	}

	/**
	 * 
	 */
	public AEMMoDServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Import OnDemand content
	 */
	@Override
	public void importOnDemandContent(Page catalog) throws ServletException,
			IOException, RequestException, RepositoryException {
		Node collectionNode = catalog.adaptTo(Node.class);
		Session session = collectionNode.getSession();

		if ((session == null) || (collectionNode == null)) {
			return;
		}

		ResourceResolver resolver = getResourceResolver(session);
		Resource dpsProjectPathResource = resolver.getResource(dpsProjectPath);
		Page page = dpsProjectPathResource.adaptTo(Page.class);

		DPSProject dpsProject = (DPSProject) AEMMoDUtil.getDPSObject(page);
		AEMMobileClient aemMobileClient = new AEMMobileClient(resolver,
				slingRequestProcessor, adapterManager);

		aemMobileClient.importOnDemandContent(dpsProject.getPath(),
				"collections");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adobe.demo.wetelco.mobile.dps.services.AEMMoDService#createCollection
	 * (com.day.cq.wcm.api.Page)
	 */
	@Override
	public Page createCollection(Page sectionPage) throws Exception {
		Page newCollectionPage = null;

		try {
			// 1. First get the node location for WeTelco AEM Mobile
			Node collectionNode = sectionPage.adaptTo(Node.class);
			Session session = collectionNode.getSession();

			if ((session == null) || (collectionNode == null)) {
				return null;
			}

			ResourceResolver resolver = getResourceResolver(session);
			PageManager pageManager = resolver.adaptTo(PageManager.class);
			Resource dpsProjectPathResource = resolver
					.getResource(dpsProjectPath);
			Page page = dpsProjectPathResource.adaptTo(Page.class);
			DPSProject dpsProject = (DPSProject) AEMMoDUtil.getDPSObject(page);
			AEMMobileClient aemMobileClient = new AEMMobileClient(resolver,
					slingRequestProcessor, adapterManager);

			String collectionFolder = dpsProjectPath + COLLECTIONS_FOLDER;
			newCollectionPage = getCollectionPageIfExists(session,
					collectionNode, sectionPage);

			if (newCollectionPage != null) {
				return newCollectionPage;
			}

			createFolderIfNotExists(collectionFolder, session);

			// Get the title and name to use for the collection
			final String title = sectionPage.getTitle().toUpperCase();
			final String name = sectionPage.getName().toUpperCase();

			newCollectionPage = pageManager.create(collectionFolder, name,
					COLLECTION_TEMPLATE, title);
			// TODO : Set a product ID
			Node newCollectionNode = newCollectionPage.adaptTo(Node.class);
			Node newCollectionNodeJcrContent = newCollectionNode
					.getNode("jcr:content");

			String hardCodedLayoutMap[] = null; // new String[2]; // TODO: Dynamic
															// fetch:getLayout(aemMobileClient,
															// dpsProject)
			//hardCodedLayoutMap[0] = "4-Col";
			//hardCodedLayoutMap[1] = "/publication/e0bb1e8d-4c5d-4278-aa55-b4468cf341aa/layout/d30f8678-d10e-c86c-e214-4026d61b20a5;version=1455595593036";

			hardCodedLayoutMap = getLayout(aemMobileClient, dpsProject);
			
			ContentCreationUtil.updateCollection(newCollectionNodeJcrContent,
					name, DPS_IMAGE, DPS_BG_IMAGE, hardCodedLayoutMap);

			// Now we need to try and associate to collection parent
			Page sectionPageParent = sectionPage.getParent();
			Node sectionPageParentNode = sectionPageParent.adaptTo(Node.class);

			if (sectionPageParentNode != null
					&& sectionPageParentNode.hasNode("jcr:content")) {
				Node categoryPageJcrNode = sectionPageParentNode
						.getNode("jcr:content");

				javax.jcr.Property commerceTypeProperty = categoryPageJcrNode
						.getProperty("cq:commerceType");
				// String valueOfCommerceType =
				// commerceTypeProperty.getString();

				if (commerceTypeProperty != null
						&& StringUtils.isNotBlank(commerceTypeProperty
								.getString())
						&& commerceTypeProperty.getString().equals("section")) {
					final String categoryPageTitle = sectionPageParent
							.getName().toUpperCase();

					if (categoryPageTitle.equals("SHOP")) {
						newCollectionNodeJcrContent.setProperty(
								"collectionTitle", "Shop");
						newCollectionNodeJcrContent.setProperty(
								"collectionCat", "Shop");
					} else {
						newCollectionNodeJcrContent.setProperty(
								"collectionTitle", categoryPageTitle);
						newCollectionNodeJcrContent.setProperty(
								"collectionCat", categoryPageTitle);
					}

					// Now let us try upload
					session.save();

					Node parentPageNode = newCollectionNodeJcrContent
							.getParent();

					Resource newCollectionPageResource = resolver
							.getResource(parentPageNode.getPath());
					Page collectionPageToUpload = newCollectionPageResource
							.adaptTo(Page.class);

					// Upload to AEM MoD
					DPSEntity dpsCollectionPage = (DPSEntity) AEMMoDUtil
							.getDPSObject(collectionPageToUpload);
					aemMobileClient.upload(dpsCollectionPage.getPath(),
							getTargetCollection(dpsCollectionPage));

					log.error("Items uploaded to AEM MoD --- ",
							dpsCollectionPage.getTitle());

				}
			}

		} catch (Exception e) {
			log.error("Error creating collection", e);
		}

		return newCollectionPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adobe.demo.wetelco.mobile.dps.services.AEMMoDService#createArticle
	 * (com.day.cq.wcm.api.Page)
	 */
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
			Resource dpsProjectPathResource = resolver
					.getResource(dpsProjectPath);
			Page page = dpsProjectPathResource.adaptTo(Page.class);
			DPSProject dpsProject = (DPSProject) AEMMoDUtil.getDPSObject(page);
			AEMMobileClient aemMobileClient = new AEMMobileClient(resolver,
					slingRequestProcessor, adapterManager);

			String articleFolder = dpsProjectPath + ARTICLES_FOLDER;
			final String name = productPage.getName().toUpperCase();
			final String title = productPage.getTitle().toUpperCase();
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

			String entityPath = articleFolder + "/" + name;
			Boolean isBanner = false;
			String md5 = Base64.encodeBase64String(DigestUtils
					.md5(new StringBufferInputStream(entityPath)));
			Node entityContentNode = null;
			String imagePath = DPS_IMAGE;

			if (!session.nodeExists(entityPath)) {
				Node entityNode = JcrUtil.copy(articleNode,
						articleFolderResourceNode, name);
				newArticle = pageManager.getPage(entityNode.getPath());
				entityContentNode = entityNode.getNode("jcr:content");
				entityContentNode.setProperty("sling:resourceType",
						"weTelco/weTelco/components/pages/article");
				entityContentNode.setProperty("cq:template",
						"/apps/weTelco/weTelco/templates/article-page");

				Node newlyCreatedArticleNodeProduct = entityNode
						.getNode("jcr:content/content-par/ng-product");
				newlyCreatedArticleNodeProduct.setProperty(
						"sling:resourceType",
						"weTelco/weTelco/components/article");
				Node newlyCreatedArticleNodeProductImge = entityNode
						.getNode("jcr:content/content-par/ng-product/image");
				Node newlyCreatedImageNodeRenamedForArticle = JcrUtil.copy(
						newlyCreatedArticleNodeProductImge,
						newlyCreatedArticleNodeProduct, "article-image");
				newlyCreatedImageNodeRenamedForArticle.setProperty(
						"sling:resourceType", "foundation/components/image");
				javax.jcr.Property productImagePath = newlyCreatedImageNodeRenamedForArticle
						.getProperty("fileReference");

				if (productImagePath != null) {
					imagePath = productImagePath.getString();
				}
				newlyCreatedArticleNodeProductImge.remove();

				if (isBanner) {
					ContentCreationUtil.updateBanner(entityContentNode, md5,
							title, title, imagePath);
				} else {
					ContentCreationUtil.updateArticle(entityContentNode, md5,
							title, title, "launch", title, title, "0",
							"feature1", "feature2", "feature3", imagePath);
				}
			} else {
				Node entityNode = session.getNode(entityPath);
				newArticle = pageManager.getPage(entityNode.getPath());
				entityContentNode = entityNode.getNode("jcr:content");

				if (isUpdated(entityContentNode, md5)) {
					resetNode(entityContentNode);
					log.info("UPDATED : " + entityPath);
					if (isBanner) {
						ContentCreationUtil.updateBanner(entityNode, md5,
								title, title, imagePath);
					} else {
						ContentCreationUtil.updateArticle(entityNode, md5,
								title, title, "launch", title, title, "",
								"feature1", "feature2", "feature3", imagePath);
					}
				} else {
					log.info("SKIPPING UPDATE: " + entityPath);
				}
			}

			// Now set some key properties

			// Make sure you can link to a collection
			/***********/
			Page categoryPage = productPage.getParent();
			Node categoryPageNode = categoryPage.adaptTo(Node.class);

			if (categoryPageNode != null
					&& categoryPageNode.hasNode("jcr:content")) {
				Node categoryPageJcrNode = categoryPageNode
						.getNode("jcr:content");

				javax.jcr.Property commerceTypeProperty = categoryPageJcrNode
						.getProperty("cq:commerceType");
				// String valueOfCommerceType =
				// commerceTypeProperty.getString();

				if (commerceTypeProperty != null
						&& StringUtils.isNotBlank(commerceTypeProperty
								.getString())
						&& commerceTypeProperty.getString().equals("section")) {
					final String categoryPageTitle = categoryPage.getName()
							.toUpperCase();
					// final String categoryPageName = categoryPage.getName();

					entityContentNode.setProperty("collectionTitle",
							categoryPageTitle);
					entityContentNode.setProperty("collectionCat",
							categoryPageTitle);

					// Now let us try upload
					session.save();

					Node parentPageNode = entityContentNode.getParent();

					Resource newArticlePageResource = resolver
							.getResource(parentPageNode.getPath());
					Page articlePageToUpload = newArticlePageResource
							.adaptTo(Page.class);

					// Upload to AEM MoD
					DPSEntity dpsArticlePage = (DPSEntity) AEMMoDUtil
							.getDPSObject(articlePageToUpload);
					aemMobileClient.upload(dpsArticlePage.getPath(),
							getTargetCollection(dpsArticlePage));

					log.error("Items uploaded to AEM MoD --- ",
							dpsArticlePage.getTitle());

				}
			}

			log.info("Created Article - " + entityContentNode.getPath());
		} catch (Exception e) {
			log.error("Error creating article", e);
		}
		return newArticle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adobe.demo.wetelco.mobile.dps.services.AEMMoDService#addBanner(com
	 * .day.cq.wcm.api.Page)
	 */
	@Override
	public void addBanner(Page collectionPage) throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adobe.demo.wetelco.mobile.dps.services.AEMMoDService#upload(com.day
	 * .cq.wcm.api.Page, java.lang.String)
	 */
	@Override
	public void upload(Page dpsPage, String parentCollectionName) {
		// TODO Auto-generated method stub

	}

	/**
	 * Get the given Collection page
	 * 
	 * @param session
	 * @param collectionNode
	 * @param sectionPage
	 * @return
	 * @throws Exception
	 */
	private Page getCollectionPageIfExists(Session session,
			Node collectionNode, Page sectionPage) throws Exception {
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

	/**
	 * Create Collection page if it doesn't exist.
	 * 
	 * @param folderPath
	 * @param session
	 * @return
	 * @throws Exception
	 */
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

	public String[] getLayout(AEMMobileClient aemMobileClient,
			DPSProject dpsProject) throws JSONException, DPSException {
		String[] layout = null;

		if (layout == null) {
			layout = ContentCreationUtil.getLayout(aemMobileClient, dpsProject);
		}
		return layout;
	}

	private boolean isUpdated(Node articleContentNode, String md5)
			throws RepositoryException {
		if (articleContentNode.hasProperty("importMD5")) {
			return !md5.equals(articleContentNode.getProperty("importMD5")
					.getString());
		} else {
			return true;
		}
	}

	private void resetNode(Node node) throws RepositoryException {
		PropertyIterator propertyIterator = node.getProperties();
		while (propertyIterator.hasNext()) {
			javax.jcr.Property property = propertyIterator.nextProperty();
			String name = property.getName();
			if (!name.equals("dps-id")
					&& !property.getDefinition().isProtected()) {
				property.setValue((Value) null);
			}
		}
		NodeIterator nodeIterator = node.getNodes();
		while (nodeIterator.hasNext()) {
			Node childNode = nodeIterator.nextNode();
			childNode.remove();
		}
		node.getSession().save();
	}

	private String getTargetCollection(DPSEntity dpsEntity)
			throws RepositoryException {
		Page page = dpsEntity.adaptTo(Page.class);
		return page.getProperties().get(PN_TARGET_COLLECTION, String.class);
	}

}
