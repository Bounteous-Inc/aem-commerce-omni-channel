/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2016 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
package com.adobe.demo.wetelco.mobile.dps.services;

import com.adobe.cq.mobile.dps.DPSEntity;
import com.adobe.cq.mobile.dps.DPSException;
import com.adobe.cq.mobile.dps.DPSProject;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.AEMMobileClient;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.RequestException;
import com.adobe.demo.wetelco.mobile.dps.utils.AdobeCaresUtil;
import com.adobe.demo.wetelco.mobile.dps.utils.ContentCreationUtil;
import com.day.cq.wcm.api.Page;

import org.apache.sling.commons.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.List;

/**
 * Upload content from an AEM Mobile Project to AEM Mobile On-Demand Services
 */
public class Uploader {
    private static final Logger LOGGER = LoggerFactory.getLogger(Uploader.class);

    private static final String PN_TARGET_COLLECTION = "collectionCat";

    private DPSProject dpsProject = null;
    private AEMMobileClient aemMobileClient = null;

    public Uploader(DPSProject dpsProject, AEMMobileClient aemMobileClient){
        this.dpsProject = dpsProject;
        this.aemMobileClient = aemMobileClient;
    }

    public int upload() throws Exception {
        int uploaded = 0;

        // Import collections
        aemMobileClient.importOnDemandContent(dpsProject.getPath(), "collections");
        if(dpsProject.getCollection(ContentCreationUtil.PARENT_COLLECTION_NAME) == null){
            Node productCollection = createRootProductCollection(ContentCreationUtil.PARENT_COLLECTION_NAME, "/content/dam/adobe-cares/application/iconiOS-180.png");
            aemMobileClient.upload(productCollection.getPath(), null);
        }

        // Upload content
        uploaded = uploaded + processContent(dpsProject.getCollections());
        uploaded = uploaded + processContent(dpsProject.getArticles());
        uploaded = uploaded + processContent(dpsProject.getBanners());

        if(uploaded>0){
            aemMobileClient.preview(dpsProject.getPath());
        }
        return uploaded;
    }

    private int processContent(List<? extends DPSEntity> entities) throws RepositoryException, ServletException, IOException, RequestException {
        int uploaded = 0;
        for (DPSEntity entity : entities) {
            if (AdobeCaresUtil.needsUpload(entity)) {
                uploaded++;
                aemMobileClient.upload(entity.getPath(), getTargetCollection(entity));
            } else {
                LOGGER.info("Skipping upload for " + entity.getPath());
            }
        }
        return uploaded;
    }

    private String getTargetCollection(DPSEntity dpsEntity) throws RepositoryException {
        Page page = dpsEntity.adaptTo(Page.class);
        return page.getProperties().get(PN_TARGET_COLLECTION, String.class);
    }

    private Node createRootProductCollection(String collectionName, String imageDirectory) throws RepositoryException, JSONException, DPSException {
        Node appNode = dpsProject.adaptTo(Node.class);
        Node collectionsParentNode = appNode.getNode("collections");
        Node collectionNode = collectionsParentNode.addNode(collectionName, "cq:Page");
        Node collectionContentNode = collectionNode.addNode("jcr:content", "cq:PageContent");
        LOGGER.info("CREATED COLLECTION: " + collectionName);

        String[] layout = ContentCreationUtil.getLayout(aemMobileClient, dpsProject);
        ContentCreationUtil.updateCollection(collectionContentNode, collectionName, imageDirectory, layout);
        return collectionNode;
    }
}
