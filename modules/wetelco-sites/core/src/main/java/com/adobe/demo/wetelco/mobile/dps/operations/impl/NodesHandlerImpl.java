package com.adobe.demo.wetelco.mobile.dps.operations.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.flat.TreeTraverser;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.demo.wetelco.mobile.dps.AEMMobileDPSCatalogStyle;
import com.adobe.demo.wetelco.mobile.dps.AEMMobileDPSPage;
import com.adobe.demo.wetelco.mobile.dps.Constants;
import com.adobe.demo.wetelco.mobile.dps.operations.CreateArticleService;
import com.adobe.demo.wetelco.mobile.dps.operations.DPSCommon;
import com.adobe.demo.wetelco.mobile.dps.operations.NodesHandler;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Created by Daniel on 17/08/15.
 */
@Component( name="Handle AEM Mobile DPS Catalogs/APPS",
        metatype = true, immediate = true)
@Property(name="service.description", value="AEM Mobile DPS Catalog Nodes/App Nodes etc handler service")
@Service
public class NodesHandlerImpl implements NodesHandler {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Reference
    private DPSCommon dpsCommon;

    @Reference
    private CreateArticleService createArticleService;

    @Reference
    protected ResourceResolverFactory resourceResolverFactory;


    @Override
    public void handleCatalogNode(Node catalogNode, UPLOAD upload, PUBLISH publish) {
        try {
            if(!catalogNode.hasProperty(Constants.PROP_DIO_TYPE)){
                return;
            }

            Iterator<Node> catalogIterator = TreeTraverser.nodeIterator(catalogNode);

            while (catalogIterator.hasNext()) {
                handleCatalogFolder(catalogIterator.next(), upload, publish);
            }
        } catch (Exception e) {
            log.error("Error while traversing catalog node", e);
        }
    }

    @Override
    public Resource getCatalogTopLevelResource(Resource catalogResource) {
        Resource result = catalogResource;
        while (!result.getValueMap().get(Constants.PROP_DIO_TYPE, "").equals(Constants.DIO_TYPE_CATALOG)
                || result.getValueMap().get(Constants.PROP_DIO_SEASON) == null ) {
            result = result.getParent();
        }
        return result;
    }

    @Override
    public String getDefaultColorway(Page articlePage) {
        ResourceResolver resolver = null;
        String colorway = articlePage.getProperties().get("colorway", String.class);
        String catalogPath = articlePage.getProperties().get("dioStyle", String.class);
        Resource dioStyleResource, armouryStyleResource;

        if (colorway != null && !colorway.equals("")) {
            return colorway;
        } else if (catalogPath != null) {
            try {
                resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
                dioStyleResource = resolver.getResource(catalogPath);
                if (dioStyleResource == null) {
                    return "";
                }

                String armouryPath = getArmouryPathForCatalogStyle(dioStyleResource);
                if (armouryPath == null) {
                    return "";
                }

                armouryStyleResource = resolver.getResource(armouryPath);

                if (armouryStyleResource == null) {
                    return "";
                }

                return getDefaultColorway(armouryStyleResource, dioStyleResource);

            } catch (Exception e) {
                log.error("Couldn't find default colorway for catalog: " + catalogPath, e);
            }
        }

        return "";
    }

    private String getDefaultColorway(Resource armouryStyleResource, Resource catalogStyleResource) {
        Map<String, ValueMap> allowedColorways = getAllowedColorways(catalogStyleResource);
        return getDefaultColorway(armouryStyleResource, allowedColorways);
    }

    @Override
    public String getDefaultColorway(Resource armouryStyleResource, Map<String, ValueMap> allowedColorways) {
        String defaultColorway = "";
        try {
            for (Resource res : armouryStyleResource.getChildren()) {
                if (allowedColorways.containsKey(res.getName())) {
                    defaultColorway = res.getName();
                }
            }
        } catch (Exception e) {
            log.error("Could not get default colorway for given catalogNode");
        }
        return defaultColorway;
    }

    @Override
    public Map<String, ValueMap> getAllowedColorways(Resource catalogResource) {
        final ResourceResolver resolver = catalogResource.getResourceResolver();
        Map<String, ValueMap> colorways = new HashMap<String, ValueMap>();
        String colorwayName = null;

        try{
            for (Resource colorway : catalogResource.getChildren()) {
                if (!dpsCommon.isColorwayNode(colorway) || isColorwayOfStyleDropped(resolver, colorway)) {
                    continue;
                }

                colorwayName = dpsCommon.getColorwayNumber(colorway);

                colorways.put(colorwayName, colorway.getValueMap());
            }
        }catch(Exception e){
            log.error("Error getting allowed colorways", e);
        }

        return colorways;
    }

    @Override
    public String getArmouryPathForCatalogStyle(Resource catalogNode) {
        return getArmouryPathForCatalogStyle(catalogNode.adaptTo(Node.class));
    }

    private String getArmouryPathForCatalogStyle(Node catalogNode) {
        String dioArmouryPath = createArticleService.getDioArmouryPath();
        String styleNumber;

        try {
            if (!catalogNode.hasProperty(Constants.PROP_DIO_TYPE)) {
                return null;
            }
            if (!catalogNode.getProperty(Constants.PROP_DIO_TYPE).getString().equals(Constants.DIO_TYPE_ARTICLE)) {
                return null;
            }

            styleNumber = catalogNode.getName();

            return dioArmouryPath + "/style/" + styleNumber.substring(0, 3) + "/" + styleNumber.substring(0, 5) + "/" + styleNumber;

        } catch (RepositoryException e) {
            log.error("Could not find armoury path for catalog node", e);
        }

        return null;
    }

    @Override
    public void handleMobileappNode(Node mobileappNode) {
        ResourceResolver resolver = null;

        try {
            resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            Session session =  resolver.adaptTo(Session.class);
            Resource articles = resolver.getResource(mobileappNode.getPath()).getChild(Constants.NN_ARTICLES);
            Resource collections = resolver.getResource(mobileappNode.getPath()).getChild(Constants.NN_COLLECTIONS);

            if (articles == null) {
                log.warn("No articles folder in mobile app. " + mobileappNode.getPath());
                return;
            }

            ValueMap vm = null;

            String articleCatalogPath = null, etcStylePath, collectionCatalogPath;

            int counter = 0;

            for (Resource articleResource : articles.getChildren()) {
                vm = articleResource.getChild("jcr:content").getValueMap();

                articleCatalogPath = vm.get("dioStyle", "");

                if (session.nodeExists(articleCatalogPath) && !isArticleStyleDropped(resolver, articleCatalogPath)) {
                    updateArticle(articleResource.adaptTo(Page.class));
                } else {
                    log.debug("Article queued for removal. path: " + articleCatalogPath);
                    createArticleService.delete(articleResource.adaptTo(Page.class));
                }
            }

            if (collections == null) {
                log.warn("No collections folder in mobile app. " + mobileappNode.getPath());
                return;
            }

            for (Resource collectionResource : collections.getChildren()) {
                vm = collectionResource.getChild("jcr:content").getValueMap();

                collectionCatalogPath = vm.get("dioStyle", "");

                if (collectionCatalogPath.isEmpty() || session.nodeExists(collectionCatalogPath)) {
                    updateCollection(collectionResource.adaptTo(Page.class));
                    continue;
                }

                log.debug("Collection queued for removal. path: " + collectionCatalogPath);
                createArticleService.delete(collectionResource.adaptTo(Page.class));
            }

            session.save();
        } catch (Exception e) {
            log.error("Error while traversing DPS articles", e);
        }
    }



    private String getSeason(String articleStylePath) {
        return articleStylePath.substring(createArticleService.getDPSCatalogsPath().length() + 1)
                                .split("/")[0];
    }

    private boolean isColorwayOfStyleDropped(ResourceResolver resolver, Resource colorway)
                                        throws Exception{
        boolean isDropped = true;

        if (colorway == null) {
            log.debug("isColorwayOfStyleDropped: colorway is null");
            return isDropped;
        }

        String etcStylePrefix = createArticleService.getDioArmouryPath() + "/style";
        String etcStylePath = dpsCommon.getEtcStylePath(colorway.getParent().getPath(), etcStylePrefix);
        String etcColorwayPath = etcStylePath + "/" + dpsCommon.getColorwayNumber(colorway);

        if (StringUtils.isEmpty(etcColorwayPath)) {
            log.debug("isColorwayOfStyleDropped: etc colorway path is empty. colorway: " + colorway.getPath());
            return isDropped;
        }

        Resource etcCWResource = resolver.getResource(etcColorwayPath);

        if (etcCWResource == null) {
            log.debug("isColorwayOfStyleDropped: etc entry doesn't exist. colorway: " + colorway.getPath() + ", etc: " + etcColorwayPath);
            return isDropped;
        }

        isDropped = false;


        String etcCWSeasonStatus = null;

        Resource cwSeasonNode = null;
        ValueMap vm = null;

        AEMMobileDPSCatalogStyle uaCatalogStyle = colorway.getParent().adaptTo(AEMMobileDPSCatalogStyle.class);
//        final String season = uaCatalogStyle.getSeason();
//
//        cwSeasonNode = etcCWResource.getChild(season);
//
//        if(cwSeasonNode != null) {
//            vm = cwSeasonNode.getValueMap();
//
//            etcCWSeasonStatus = vm.get(Constants.CW_STATUS, Constants.ACTIVE);
//            isDropped = etcCWSeasonStatus.trim().equalsIgnoreCase(Constants.DROPPED);
//        } else {
//            log.debug("isColorwayOfStyleDropped: season doesn't exist. colorway: " + colorway.getPath() + ", etc: " + etcColorwayPath + ", " + season);
//        }
        return isDropped;
    }

    private Resource findFirstColorwayOfStyleInEtc(ResourceResolver resolver, String articleStylePath,
                            Resource etcStyleResource) throws Exception{
        Resource articleStyleResource = resolver.getResource(articleStylePath);
        Resource etcColorwayNode = null;

        if(articleStyleResource == null){
            return etcColorwayNode;
        }

        Iterator<Resource> articleStyleItr = articleStyleResource.getChildren().iterator();

        Resource styleColorwayNode = null;

        while(articleStyleItr.hasNext()){
            styleColorwayNode = articleStyleItr.next();

            if(!dpsCommon.isColorwayNode(styleColorwayNode)){
                continue;
            }

            break;
        }

        if(styleColorwayNode == null){
            return etcColorwayNode;
        }

        String styleColorwayNumber = dpsCommon.getColorwayNumber(styleColorwayNode);

        Iterator<Resource> etcStyleItr =  etcStyleResource.getChildren().iterator();

        while(etcStyleItr.hasNext()){
            etcColorwayNode = etcStyleItr.next();

            if(!etcColorwayNode.getName().equals(styleColorwayNumber)){
                continue;
            }

            break;
        }

        return etcColorwayNode;
    }

    public boolean isArticleStyleDropped(ResourceResolver resolver, String articleStylePath)
                                            throws Exception{
        boolean isDropped = true;

        String etcStylePrefix = createArticleService.getDioArmouryPath() + "/style";
        String etcStylePath = dpsCommon.getEtcStylePath(articleStylePath, etcStylePrefix );

        if (StringUtils.isEmpty(etcStylePath)) {
            log.debug("isArticleStyleDropped: etc path is empty for style: " + articleStylePath);
            return isDropped;
        }

        Session session =  resolver.adaptTo(Session.class);

        if (!session.nodeExists(etcStylePath)) {
            log.debug("isArticleStyleDropped: etc path doesn't exist for style: " + articleStylePath);
            return isDropped;
        }

        isDropped = false;

        Resource etcStyleResource = resolver.getResource(etcStylePath);
        Resource cwNodeInEtc = null;

        Resource cwSeasonNode = null;
        ValueMap vm = null;

        Iterator<Resource> itr =  etcStyleResource.getChildren().iterator();
        String styleStatus = null;

        if(!itr.hasNext()){
            log.debug("isArticleStyleDropped: there are no colorways in etc for style: " + articleStylePath + ", etc: " + etcStylePath);
            return isDropped;
        }

        cwNodeInEtc = findFirstColorwayOfStyleInEtc(resolver, articleStylePath, etcStyleResource);

        if(cwNodeInEtc == null){
            log.debug("isArticleStyleDropped: no colorway node in etc for style: " + articleStylePath + ", etc: " + etcStylePath);
            return isDropped;
        }

        final String season = getSeason(articleStylePath);

        cwSeasonNode = cwNodeInEtc.getChild(season);

        if(cwSeasonNode != null) {
            vm = cwSeasonNode.getValueMap();

            styleStatus = vm.get(Constants.STYLE_STATUS, Constants.ACTIVE);

            isDropped = styleStatus.trim().equalsIgnoreCase(Constants.DROPPED);
        } else {
            log.debug("isArticleStyleDropped: there is no season entry in etc for style: " + articleStylePath + ", etc: " + etcStylePath + ", season: " + season);
        }

        return isDropped;
    }

    public Page createCollection(Node catalogNode, UPLOAD upload, PUBLISH publish){
        String colPath = null;
        Page collectionPage = null;

        boolean update = false;

        Calendar cal = Calendar.getInstance();
        PageManager pageManager;

        try{
            colPath = catalogNode.getPath();
            log.debug("Create collection " + colPath);

            Node parent = catalogNode.getParent();

            if (!catalogNode.hasProperty(Constants.PROP_DIO_SEASON) && parent != null && parent.hasProperty(Constants.PROP_DIO_TYPE)) {
                log.debug("createCollection: create parent collection. collection: " + catalogNode.getPath() + ", parent: " + parent.getPath());
                createCollection(parent, upload, publish);
            }

            Session session = catalogNode.getSession();
            collectionPage = createArticleService.getCollectionPageIfExists(session, catalogNode);

            if (collectionPage == null) {
                collectionPage = createArticleService.createCollection(session, catalogNode);
                update = true;
            }

            if (collectionPage == null) {
                log.error("Error while creating new collectionPage - " + catalogNode.getPath());
                return null;
            }

            if (!catalogNode.hasProperty(Constants.PROP_DIO_MOD_DATE)) {
                catalogNode.setProperty(Constants.PROP_DIO_MOD_DATE, cal);
            }

            if (UPLOAD.FORCE.equals(upload)) {
                update = true;
            }

            if (!update && !isCollectionUpdated(catalogNode, collectionPage)) {
                return collectionPage;
            }

            pageManager = collectionPage.getPageManager();
            pageManager.touch(collectionPage.adaptTo(Node.class), true, cal, false);

            updateDPSMetadata(collectionPage);
            session.save();

            String path = collectionPage.getPath();

            if (upload.equals(UPLOAD.TRUE) || upload.equals(UPLOAD.FORCE)) {
                log.info("DPS Collection upload {}", path);
                createArticleService.upload(collectionPage, true);
            }

            if (publish.equals(PUBLISH.TRUE) || publish.equals(PUBLISH.FORCE)) {
                log.trace("DPS Article publish {}", path);
                createArticleService.publish(collectionPage, true);
            }

            session.save();
        }catch(Exception e){
            log.error("Error creating collection - " + colPath, e);
        }

        log.info("COLLECTION UPDATE, UPLOAD, PUBLISH DONE - " + colPath);

        return collectionPage;
    }
    private Date tryParse(String dateString, String[] formatStrings) {
        for (String formatString : formatStrings)
        {
            try
            {
                return new SimpleDateFormat(formatString).parse(dateString);
            }
            catch (ParseException e) {}
        }

        return null;
    }

    private boolean isCollectionUpdated(Node catalogNode, Page collectionPage)  throws RepositoryException {
        Calendar catalogModified, catalogJcrModified, collectionModified;

        catalogModified = catalogNode.getProperty(Constants.PROP_DIO_MOD_DATE).getDate();
        if (catalogNode.hasProperty(JcrConstants.JCR_LASTMODIFIED)) {
            catalogJcrModified = catalogNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate();
            if (catalogJcrModified.after(catalogModified)) {
                catalogModified = catalogJcrModified;
            }
        }

        collectionModified = collectionPage.getLastModified();

        if (collectionModified.before(catalogModified)) {
            log.debug("Collection modified before catalog node... " + catalogNode.getPath());
            return true;
        }

        return false;
    }

    private boolean isArticleUpdated(Node catalogNode, Page articlePage) throws RepositoryException {
        Calendar catalogModified, catalogJcrModified, articleModified;
        AEMMobileDPSPage dpsPage = articlePage.adaptTo(AEMMobileDPSPage.class);

        Resource catalogResource = getArticlePageCatalog(articlePage);

        if (catalogResource == null) {
            log.warn("Checking isArticleUpdated null catalog resource " + articlePage.getPath());
            return false;
        }

        AEMMobileDPSCatalogStyle catalogStyle = catalogResource.adaptTo(AEMMobileDPSCatalogStyle.class);
        catalogModified = catalogNode.getProperty(Constants.PROP_DIO_MOD_DATE).getDate();

        if (catalogNode.hasProperty(JcrConstants.JCR_LASTMODIFIED)) {
            catalogJcrModified = catalogNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate();

            if (catalogJcrModified.after(catalogModified)) {
                catalogModified = catalogJcrModified;
            }
        }

        articleModified = articlePage.getLastModified();

        if (articleModified.before(catalogModified)) {
            log.debug("Article modified before catalog node... " + catalogNode.getPath());
            return true;
        }

//        final String armouryModString = catalogStyle.getArmouryLastModified();
//        String pageArmouryModString = dpsPage.getArmouryLastModified();
//
//        log.debug("Compare " + armouryModString + ", " + pageArmouryModString);
//
//        if (!armouryModString.equals(pageArmouryModString)) {
//            log.debug("Article modified before armoury node... " + catalogNode.getPath() + ", " + armouryModString + " != " + pageArmouryModString);
//            dpsPage.setArmouryLastModified(armouryModString);
//            return true;
//        }
//
//        if (createArticleService.getIsAutoPublishEnabled() && !dpsPage.isPublished()) {
//            log.debug("isArticleUpdated: article should be published but isn't");
//            createArticleService.publish(articlePage);
//        }
//
//        try {
//            String thumbPath = createArticleService.getArticleThumbnailPath(catalogResource);
//
//            if (StringUtils.isEmpty(thumbPath)) {
//                thumbPath = CreateArticleServiceImpl.THUMBNAIL_430_430_PLACEHOLDER;
//            }
//
//            //log.trace("Article card image styleNumber: " + catalogStyle.getStyleNumber() + ", old thumb: " + dpsPage.getArticleCardImage() + ", new thumb: " + thumbPath);
//
//            if (!StringUtils.equals(thumbPath, dpsPage.getArticleCardImage())) {
//                log.debug("isArticleUpdated: thumbnail updated. old=" + dpsPage.getArticleCardImage() + ", new=" + thumbPath);
//                return true;
//            }
//
//        } catch (Exception e) {
//            log.warn("Couldn't check current article card image for " + catalogResource.getPath(), e);
//        }

        return false;
    }

    private Resource getArticlePageCatalog(Page articlePage) {
        final ValueMap vm = articlePage.getContentResource().getValueMap();
        final String articleCatalogPath = vm.get("dioStyle", "");
        final ResourceResolver resolver = articlePage.adaptTo(Resource.class).getResourceResolver();

        if (articleCatalogPath.isEmpty()) {
            log.debug("updateArticle: empty catalog path. article: " + articlePage.getPath());
            return null;
        }
        final Resource catalogResource = resolver.getResource(articleCatalogPath);
        if (catalogResource == null) {
            log.debug("updateArticle: catalog path doesn't exist. article: " + articlePage.getPath());
            return null;
        }
        return catalogResource;
    }

    private void updateDPSMetadata(Page dpspage) {
        final Resource catalogResource = getArticlePageCatalog(dpspage);
        final AEMMobileDPSCatalogStyle catalogStyle = catalogResource.adaptTo(AEMMobileDPSCatalogStyle.class);
        final AEMMobileDPSPage uadpsPage = dpspage.adaptTo(AEMMobileDPSPage.class);

        if (catalogStyle == null) {
            log.warn("Invalid article page - no catalog style");
            return;
        }

        try {
            uadpsPage.updateDPSMetadata(catalogStyle);

            if (catalogStyle.isArticle()) {
                String thumbPath = createArticleService.getArticleThumbnailPath(catalogResource);

                if (StringUtils.isEmpty(thumbPath)) {
                    //log.debug("Replace thumbnail with placeholder for style " + catalogStyle.getStyleNumber());
                    uadpsPage.setArticleCardImage(CreateArticleServiceImpl.THUMBNAIL_430_430_PLACEHOLDER);
                } else if (!StringUtils.equals(thumbPath, uadpsPage.getArticleCardImage())) {
                    //log.debug("Replace thumbnail with new image " + catalogStyle.getStyleNumber() + ", " + thumbPath);
                    uadpsPage.setArticleCardImage(thumbPath);
                }
            }
        } catch (Exception e) {
            log.warn("Couldn't set DPS metadata for node: " + dpspage.getPath(), e);
        }
    }

    public void updateCollection(Page collectionPage) throws Exception {
        updateCollection(collectionPage, false);
    }

    public void updateCollection(Page collectionPage, boolean forceUpdate) throws Exception {
        final Resource catalogResource = getArticlePageCatalog(collectionPage);
        if (catalogResource == null) {
            return;
        }

        final ResourceResolver resolver = catalogResource.getResourceResolver();
        final Node catalogNode = catalogResource.adaptTo(Node.class);
        if (forceUpdate || isCollectionUpdated(catalogNode, collectionPage)) {
            Calendar cal = Calendar.getInstance();
            log.debug("updateCollection: update collection date: " + cal.toString());
            PageManager pageManager = collectionPage.getPageManager();
            pageManager.touch(collectionPage.adaptTo(Node.class), true, cal, false);
            updateDPSMetadata(collectionPage);
            resolver.commit();
            if (createArticleService.getIsAutoUploadEnabled()) {
                createArticleService.upload(collectionPage, false); // Collection, FALSE
            }

            createArticleService.updateCollectionContents(collectionPage.adaptTo(AEMMobileDPSPage.class));
        }
    }
    public void updateArticle(Page articlePage) throws Exception {
        updateArticle(articlePage, false);
    }

    public void updateArticle(Page articlePage, boolean forceUpdate) throws Exception {
        final Resource catalogResource = getArticlePageCatalog(articlePage);
        if (catalogResource == null) {
            return;
        }

        final ResourceResolver resolver = catalogResource.getResourceResolver();
        final Node catalogNode = catalogResource.adaptTo(Node.class);
        if (forceUpdate || isArticleUpdated(catalogNode, articlePage)) {
            createArticleService.updateArticle(articlePage, catalogNode);
            Calendar cal = Calendar.getInstance();
            log.debug("updateArticle: update article date: " + cal.toString());
            PageManager pageManager = articlePage.getPageManager();
            pageManager.touch(articlePage.adaptTo(Node.class), true, cal, false);
            updateDPSMetadata(articlePage);
            resolver.commit();
            if (createArticleService.getIsAutoUploadEnabled()) {
                createArticleService.upload(articlePage, false); // article, false
            }
            
            if (createArticleService.getIsAutoPublishEnabled()) {
                createArticleService.publish(articlePage);
            }
        }
    }

    public Page createArticle(Node catalogNode, UPLOAD upload, PUBLISH publish)
                                        throws Exception {
        Session session = catalogNode.getSession();
        Boolean update = false;
        PageManager pageManager;
        Calendar cal = Calendar.getInstance();
        ResourceResolver resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

        if (isArticleStyleDropped(resolver, catalogNode.getPath())) {
            log.info("createArticle: skipped dropped style: " + catalogNode.getPath());
            return null;
        }

        Node parent = catalogNode.getParent();
        if (parent != null && parent.hasProperty(Constants.PROP_DIO_TYPE)) {
            log.debug("createArticle: create parent collection. collection: " + catalogNode.getPath() + ", parent: " + parent.getPath());
            createCollection(parent, upload, publish);
        }

        Page articlePage = createArticleService.createArticle(session, catalogNode);

        if (articlePage == null) {
            log.error("Error while creating new article - " + catalogNode.getPath());
            return null;
        }

        final String articlePath = articlePage.getPath();

        if (!catalogNode.hasProperty(Constants.PROP_DIO_MOD_DATE)) {
            catalogNode.setProperty(Constants.PROP_DIO_MOD_DATE, cal);
            update = true;
        } else if (!articlePage.getContentResource().getValueMap().containsKey(Constants.PROP_DPS_TITLE)) {
            // dps metadata not set yet
            update = true;
        } else if ((publish.equals(PUBLISH.TRUE) || upload.equals(UPLOAD.TRUE))){
            update = isArticleUpdated(catalogNode, articlePage);
        }

        if (update || publish.equals(PUBLISH.FORCE)) {
            log.debug("CREATEARTICLE: UPDATE ARTICLE DATE: " + cal.toString());

            pageManager = articlePage.getPageManager();
            pageManager.touch(articlePage.adaptTo(Node.class), true, cal, false);

            updateDPSMetadata(articlePage);
        }

        session.save();

        boolean up = false, pub = false;

        if (upload.equals(UPLOAD.TRUE) && update || upload.equals(UPLOAD.FORCE)) {
            log.info("DPS Article upload {}", articlePath);

            if (articlePage.getContentResource().getValueMap().get("dps-id", "").isEmpty()) {
                createArticleService.upload(articlePage, true);
            } else {
                createArticleService.upload(articlePage, false);
            }

            up = true;
        }

        if (publish.equals(PUBLISH.TRUE) && update
                || publish.equals(PUBLISH.FORCE)) {
            log.trace("DPS Article publish {}", articlePath);
            createArticleService.publish(articlePage);
            pub = true;
        }

        log.info("ARTICLE PROCESSED - ops: " +
                (update ? " UPDATE " : "") +
                (up ? " UPLOAD " : "") +
                (pub ? " PUBLISH " : "") +
                ", article: " + catalogNode.getPath());

        return articlePage;
    }

    private void handleCatalogFolder(Node catalogNode, UPLOAD upload, PUBLISH publish) throws Exception {
        if (!catalogNode.hasProperty(Constants.PROP_DIO_TYPE)){
            return;
        }

        Session session = catalogNode.getSession();

        if (session == null) {
            log.warn("Empty session");
            return;
        }

        session.refresh(true);

        if(dpsCommon.isArticleNode(catalogNode)){
            createArticle(catalogNode, upload, publish);
        }else if(dpsCommon.isCollectionNode(catalogNode)){
            createCollection(catalogNode, upload, publish);
        }
    }

    public boolean isNewStyle(Resource catalogStyleResource) {
        AEMMobileDPSCatalogStyle uaCatalogStyle = catalogStyleResource.adaptTo(AEMMobileDPSCatalogStyle.class);

        ResourceResolver resolver = catalogStyleResource.getResourceResolver();

//////        if(uaCatalogStyle.isArticle() && StringUtils.isEmpty(armouryStylePath)) {
//////            return false;
//////        }
//////
//////        Resource armourStyleResource = resolver.getResource(armouryStylePath);
////
////        if(armourStyleResource == null) {
////            return false;
////        }
////
////        ValueMap armouryStyleVM = armourStyleResource.getValueMap();
//
//        final Map<String, ValueMap> allowedColorways = getAllowedColorways(catalogStyleResource);
//        String[] colorways = allowedColorways.keySet().toArray(new String[allowedColorways.size()]);
//
//        for (String colorway : colorways) {
//
//            final Object productLifecycle = armouryStyleVM.get(colorway + "/" + Constants.PROP_DIO_PRODUCT_LIFECYCLE);
//
//            if (productLifecycle instanceof String && productLifecycle.toString().equalsIgnoreCase(Constants.PROP_PRODUCT_LIFECYCLE_NEW)) {
//                return true;
//            } else if (productLifecycle instanceof String[]) {
//                for (String value : (String[])productLifecycle) {
//                    if (value.equalsIgnoreCase(Constants.PROP_PRODUCT_LIFECYCLE_NEW)) {
//                        return true;
//                    }
//                }
//            }
//        }

        return false;
    }
}
