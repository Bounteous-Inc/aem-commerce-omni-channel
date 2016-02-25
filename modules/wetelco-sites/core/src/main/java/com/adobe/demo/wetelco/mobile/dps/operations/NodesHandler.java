package com.adobe.demo.wetelco.mobile.dps.operations;

import java.util.Map;

import javax.jcr.Node;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.Page;

/**
 * Created by Daniel on 17/08/15.
 */
public interface NodesHandler {

    enum UPLOAD {
        /**
         * Do not upload anything
         */
        FALSE,
        /**
         * Upload only when modified
         */
        TRUE,
        /**
         * Upload all entries
         */
        FORCE
    }

    enum PUBLISH {
        /**
         * Do not publish anything
         */
        FALSE,
        /**
         * Publish only modified entries
         */
        TRUE,
        /**
         * Publish all entries
         */
        FORCE
    }

    void handleCatalogNode(Node catalogNode, UPLOAD upload, PUBLISH publish);

    Page createCollection(Node catalogNode, UPLOAD upload, PUBLISH publish);

    void updateCollection(Page collectionPage) throws Exception;

    void updateCollection(Page collectionPage, boolean forceUpdate) throws Exception;

    void updateArticle(Page articlePage, boolean forceUpdate) throws Exception;

    String getArmouryPathForCatalogStyle(Resource catalogNode);

    Resource getCatalogTopLevelResource(Resource catalogResource);

    String getDefaultColorway(Page articlePage);

    String getDefaultColorway(Resource armouryStyleResource, Map<String, ValueMap> allowedColorways);

    Map<String, ValueMap> getAllowedColorways(Resource catalogResource);

    void handleMobileappNode(Node mobileappNode);

    boolean isNewStyle(Resource catalogResource);

    public Page createArticle(Node styleAssortmentNode, UPLOAD upload, PUBLISH publish) throws Exception;

    public void updateArticle(Page articlePage) throws Exception;

    public boolean isArticleStyleDropped(ResourceResolver resolver, String articleStylePath) throws Exception;
}
