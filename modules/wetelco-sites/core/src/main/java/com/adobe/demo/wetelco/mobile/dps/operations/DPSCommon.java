package com.adobe.demo.wetelco.mobile.dps.operations;

import javax.jcr.Node;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.dam.api.Asset;

public interface DPSCommon {
    public String getDIOType(Resource resource);

    public boolean isColorwayNode(Resource resource);

    public boolean isArticleNode(Resource resource);

    public boolean isArticleNode(Node node);

    public boolean isCollectionNode(Node node);

    public boolean isCollectionNode(Resource resource);

    public boolean isCategory2Node(Resource resource);

    public boolean isFolder(Resource resource) throws Exception;

    public boolean isDamAsset(Resource resource) throws Exception;

    public boolean isValidImage(Asset asset) throws Exception;

    public boolean isWorkflowRunningOnPayload(ResourceResolver resolver, String resourcePath, String workflowPath) throws Exception;

    public String getEtcStylePath(String articleStylePath, String stylePathPrefix);

    public String getCollectionPathForArticle(String path);

    public String getColorwayNumber(Resource colorway);

    public String getStyleNumberFromB2BPath(Node b2bImage) throws Exception;
}
