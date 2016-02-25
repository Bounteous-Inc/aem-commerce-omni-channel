package com.adobe.demo.wetelco.mobile.dps.models;

import java.util.Date;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ResourcePath;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.LoggerFactory;

import com.adobe.cq.mobile.dps.DPSCollection;
import com.adobe.cq.mobile.dps.DPSEntity;
import com.adobe.cq.mobile.dps.DPSProject;
import com.adobe.cq.mobile.dps.impl.DPSConstants;
import com.adobe.demo.wetelco.mobile.dps.AEMMobileDPSCatalogStyle;
import com.adobe.demo.wetelco.mobile.dps.AEMMobileDPSPage;
import com.adobe.demo.wetelco.mobile.dps.Constants;
import com.adobe.demo.wetelco.mobile.dps.operations.CreateArticleService;
import com.adobe.demo.wetelco.mobile.dps.operations.DPSCommon;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;

@Model(adaptables = Resource.class, adapters = {AEMMobileDPSPage.class})
public class AEMMobileDPSPageImpl implements AEMMobileDPSPage {

    protected final org.slf4j.Logger log = LoggerFactory.getLogger(this.getClass());

    protected final static String PROP_ARMOURY_LAST_MODIFIED = "uaDIO:armouryLastModified";

    protected final static String PROP_RESOURCE_TYPE = SlingConstants.NAMESPACE_PREFIX + ":" + SlingConstants.PROPERTY_RESOURCE_TYPE;
    protected final static String PROP_RESOURCE_TYPE_IMAGE = "foundation/components/image";

    @OSGiService
    private CreateArticleService createArticleService;

    @OSGiService
    private DPSCommon dpsCommon;

    @Inject @Source("sling-object")
    private ResourceResolver resourceResolver;

    @SlingObject
    private Resource resource;

    private Page page;

    @Inject
    @ResourcePath(name=JcrConstants.JCR_CONTENT + "/" + "dioStyle", optional = true)
    protected AEMMobileDPSCatalogStyle catalogStyle;

    @Inject @Named(JcrConstants.JCR_CONTENT + "/" + "dioStyle") @Optional
    protected String catalogPath;

    @Inject @Named(JcrConstants.JCR_CONTENT + "/" + DPSConstants.PN_DPS_ARTICLE_ID) @Optional
    protected String dpsId;

    @Inject @Named(JcrConstants.JCR_CONTENT + "/" + PROP_ARMOURY_LAST_MODIFIED) @Optional
    protected String armouryLastModified;

    @Inject @Named(JcrConstants.JCR_CONTENT + "/" + DPSConstants.PN_DPS_LAST_DPS_PUBLISHED) @Optional
    protected Date lastPublished;

    @Inject @Named(JcrConstants.JCR_CONTENT + "/" + DPSConstants.PN_DPS_LAST_DPS_PUBLISHED_BY) @Optional
    protected String lastPublishedBy;

    @Inject @Named(JcrConstants.JCR_CONTENT + "/" + DPSConstants.PN_DPS_RESOURCE_TYPE) @Optional
    protected String dpsResourceType;

    @Inject @Named(JcrConstants.JCR_CONTENT + "/image/fileReference") @Optional
    protected String articleCardImage;


    @PostConstruct
    public void init() {
        page = resource.adaptTo(Page.class);
    }

    public String getPath() {
        return resource.getPath();
    }

    public Page getPage() {
        return page;
    }

    @Override
    public AEMMobileDPSPage getParent() {
        AEMMobileDPSCatalogStyle parentStyle;

        if (catalogStyle != null) {
            parentStyle = catalogStyle.getParent();
        } else {
            String collectionPath = dpsCommon.getCollectionPathForArticle(catalogPath);
            Resource collectionRes = resourceResolver.getResource(collectionPath);
            if (collectionRes == null) {
                return null;
            }

            parentStyle = collectionRes.adaptTo(AEMMobileDPSCatalogStyle.class);
        }

        if (parentStyle == null) {
            return null;
        }

        return parentStyle.getDPSPage();
    }

    public DPSProject getDPSProject() {
        DPSEntity dpsCollection = page.adaptTo(DPSCollection.class);
        return dpsCollection.getProject();
    }

    public String getDpsId() {
        return dpsId;
    }

    public boolean isUploaded() {
        return StringUtils.isNotEmpty(dpsId);
    }

    public String getCatalogPath() {
        return catalogPath;
    }

    public AEMMobileDPSCatalogStyle getCatalogStyle() {
        return catalogStyle;
    }

    public void setCatalogStyle(AEMMobileDPSCatalogStyle catalogStyle) {
        final ValueMap vm = page.getContentResource().adaptTo(ModifiableValueMap.class);
        vm.put(Constants.DIO_STYLE, catalogStyle.getPath());

        this.catalogStyle = catalogStyle;
    }

    public String getArmouryLastModified() {
        return armouryLastModified;
    }

    public void setArmouryLastModified(String value) {
        final ValueMap vm = page.getContentResource().adaptTo(ModifiableValueMap.class);
        vm.put(PROP_ARMOURY_LAST_MODIFIED, value);
    }

    public Date getLastPublished() {
        return lastPublished;
    }

    public String getLastPublishedBy() {
        return lastPublishedBy;
    }

    public boolean isPublished() {
        return lastPublished != null;
    }

    public void updateDPSMetadata(AEMMobileDPSCatalogStyle catalogStyle) {
        final ValueMap vm = page.getContentResource().adaptTo(ModifiableValueMap.class);
        HashMap<String, String> layout = null;
        String layoutName;

        if (catalogStyle.isArticle()) {
            vm.put(Constants.PROP_DPS_CATEGORY, catalogStyle.getDPSCategory());
            vm.put(Constants.PROP_DPS_ABSTRACT, catalogStyle.getDPSAbstract());
            vm.put(Constants.PROP_DPS_DEPARTMENT, catalogStyle.getDPSDepartment());
        } else if (catalogStyle.isCollection()) {
            vm.put(Constants.PROP_DPS_PRODUCT_IDS, catalogStyle.getDPSProductIds()); //product ids
            vm.put(Constants.PROP_DPS_PRODUCT_ID, catalogStyle.getDPSProductIds()[0]); //product ids

            DPSEntity dpsCollection = page.adaptTo(DPSCollection.class);
            DPSProject dpsProject = dpsCollection.getProject();

            if (dpsProject != null) {
                layoutName = catalogStyle.getDPSLayoutName();
                if (StringUtils.isNotEmpty(layoutName)) {
                    layout = createArticleService.getLayoutByName(dpsProject, layoutName);
                }

                if (layout == null) {
                    layout = createArticleService.getLayoutByName(dpsProject, Constants.DPS_DEFAULT_LAYOUT_NAME);
                }

                if (layout != null) {
                    vm.put("dps-layoutTitle", layout.get("title"));
                    vm.put("dps-layout", layout.get("url"));
                }
            }

            final Node collectionContentNode = page.getContentResource().adaptTo(Node.class);

            try {
                final Node imageNode = JcrUtils.getOrAddNode(collectionContentNode, "image");
                imageNode.setProperty(PROP_RESOURCE_TYPE, PROP_RESOURCE_TYPE_IMAGE);
                imageNode.setProperty("fileReference", catalogStyle.getThumbnailPath());

                final Node backgroundImageNode = JcrUtils.getOrAddNode(collectionContentNode, "background-image");
                backgroundImageNode.setProperty("fileReference", catalogStyle.getBackgroundPath());
                backgroundImageNode.setProperty("sling:resourceType", "wcm/foundation/components/image");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        vm.put(Constants.PROP_DPS_TITLE, catalogStyle.getTitle());
        vm.put(JcrConstants.JCR_TITLE, catalogStyle.getTitle());

        vm.put(Constants.PROP_DPS_ENTITY_TYPE, catalogStyle.getDPSEntityType());
        vm.put(Constants.PROP_DPS_KEYWORDS, catalogStyle.getDPSKeywords());
        vm.put(Constants.PROP_DPS_SHORT_TITLE, catalogStyle.getDPSShortTitle());
        vm.put(Constants.PROP_NAME, catalogStyle.getName());

        //vm.put(PROP_ARMOURY_LAST_MODIFIED, catalogStyle.getArmouryLastModified());
    }

    public String getArticleCardImage() {
        return articleCardImage;
    }

    public void setArticleCardImage(String thumbnailRef) {
        try {
            Node contentNode = page.getContentResource().adaptTo(Node.class);
            Node imageNode = JcrUtils.getOrAddNode(contentNode, "image");
            if (imageNode != null) {
                imageNode.setProperty(PROP_RESOURCE_TYPE, PROP_RESOURCE_TYPE_IMAGE);
                imageNode.setProperty("fileReference", thumbnailRef);
            }
        } catch (RepositoryException e) {
            log.error("Couldn't set article card image", e);
        }
    }

    public void upload(boolean addToParentCollection) {
        createArticleService.upload(page, addToParentCollection);
    }


    public void publish(boolean publishParent) {
        createArticleService.publish(page, publishParent);
    }

    public void unpublish() {
        createArticleService.unpublish(page);
    }

    public void delete() {
        createArticleService.delete(page);
    }

    public boolean isArticle() {
        return DPSConstants.DPS_TYPE_ARTICLE.equals(dpsResourceType);
    }

    public boolean isCollection() {
        return DPSConstants.DPS_TYPE_COLLECTION.equals(dpsResourceType);
    }

}
