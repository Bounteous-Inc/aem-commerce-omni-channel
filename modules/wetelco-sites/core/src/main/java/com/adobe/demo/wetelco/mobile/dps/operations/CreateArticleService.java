package com.adobe.demo.wetelco.mobile.dps.operations;

import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;

import com.adobe.cq.mobile.dps.DPSProject;
import com.adobe.demo.wetelco.mobile.dps.AEMMobileDPSPage;
import com.day.cq.wcm.api.Page;

public interface CreateArticleService {

    /* DPS related functions */

    Page getArticle(Resource catalogResource);

    /**
     * Create article entry
     * @param session
     * @param articleNode
     * @return new article page in dps app structure
     * @throws Exception
     */
    Page createArticle(Session session, Node articleNode) throws Exception;
    Page createArticle(Page productPage) throws Exception;
    
    /**
     * Update article thumbnail in article node
     * @param articlePage
     * @param articleNode
     * @throws Exception
     */
    void updateArticle(Page articlePage, Node articleNode) throws Exception;

    Page getCollectionPageIfExists(Session session, Node collectionNode) throws Exception;
    HashMap<String, String> getLayoutByName(DPSProject dpsProject, String layoutName);

    void updateCollectionContents(AEMMobileDPSPage collectionPage);

    /**
     * Create collection entry
     * @param session
     * @param collectionNode
     * @return new collection page in dps app structure
     * @throws Exception
     */
    Page createCollection(Session session, Node collectionNode) throws Exception;

    Page createCollection(Page section) throws Exception;
    
    /**
     * Add article/collection to it's parent based on assortment structure
     * @param dpsPage
     */
    void addToParentCollection(Page dpsPage);

    /**
     * Add banner to given collection
     * Banner name and id is resolved by internal logic
     * @param collectionPage
     */
    void addBanner(Page collectionPage);

    /**
     * Upload given page to DPS. Can be either article or collection
     * @param dpsPage - dps page
     * @param addToParentCollection - try to add page to parent collection. Will find out parent collection based on assortments structure
     */
    void upload(Page dpsPage, boolean addToParentCollection);

    /**
     * Publish given dps page and it's parent
     * @param dpsPage
     */
    void publish(Page dpsPage);

    /**
     * Add given dps page (article or collection) and if required it's parent to publish job
     * @param dpsPage
     * @param publishParent
     */
    void publish(Page dpsPage, boolean publishParent);

    /**
     * Publish to DPS
     *
     * @param dpsPage
     * @param publishParent
     */
    public boolean dpsPublish(Page dpsPage, boolean publishParent);

    /**
     * Check if published
     *
     * @param dpsPage
     */
    public boolean isEntityPublished(Page dpsPage);

    /**
     * Unpublish given dps page (article or collection)
     * @param dpsPage
     */
    void unpublish(Page dpsPage);

    /**
     * Delete article or collection in DPS. Remove local instance too.
     * @param dpsPage
     */
    void delete(Page dpsPage);

    /**
     * Delete article or collection in DPS. Remove local instance too.
     * @param dpsPage
     */
    void dpsDelete(Page dpsPage);

    String getArticleThumbnailPath(Resource catalogResource)
            throws Exception;

    /* Configuration Functions */

    String getDioArmouryPath();

    String getDPSCatalogsPath();

    String getDPSProjectPath();

    Boolean getIsAutoUploadEnabled();

    Boolean getIsAutoPublishEnabled();

}
