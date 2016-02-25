package com.adobe.demo.wetelco.mobile.dps.operations.impl;

import java.util.List;

import javax.jcr.Node;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.adobe.demo.wetelco.mobile.dps.Constants;
import com.adobe.demo.wetelco.mobile.dps.operations.DPSCommon;
import com.day.cq.dam.api.Asset;
import com.day.cq.workflow.exec.Workflow;
import com.day.cq.workflow.status.WorkflowStatus;

@Component
@Service
public class DPSCommonImpl implements DPSCommon {

	@Override
	public boolean isColorwayNode(Resource resource) {
		return getDIOType(resource).equals(Constants.DIO_TYPE_ARTICLE_CONTENT);
	}

	@Override
	public boolean isArticleNode(Resource resource) {
		return getDIOType(resource).equals(Constants.DIO_TYPE_ARTICLE);
	}

	@Override
	public boolean isArticleNode(Node node) {
		boolean isArticle = false;

		if (node == null) {
			return isArticle;
		}

		try {
			if (node.hasProperty(Constants.PROP_DIO_TYPE)) {
				isArticle = node.getProperty(Constants.PROP_DIO_TYPE).getString().equals(Constants.DIO_TYPE_ARTICLE);
			}
		} catch (Exception e) {
		}

		return isArticle;
	}

	public boolean isCollectionNode(Resource resource) {
		return isCollectionNode(resource.adaptTo(Node.class));
	}

	@Override
	public boolean isCollectionNode(Node node) {
		boolean isCollection = false;

		if (node == null) {
			return isCollection;
		}

		try {
			if (!node.hasProperty(Constants.PROP_DIO_TYPE)) {
				return false;
			}

			String type = node.getProperty(Constants.PROP_DIO_TYPE).getString();

			isCollection = type.equals(Constants.DIO_TYPE_CATALOG) || type.equals(Constants.DIO_TYPE_CATEGORY_1)
					|| type.equals(Constants.DIO_TYPE_CATEGORY_2);
		} catch (Exception e) {
		}

		return isCollection;
	}

	@Override
	public boolean isCategory2Node(Resource resource) {
		return getDIOType(resource).equals(Constants.DIO_TYPE_CATEGORY_2);
	}

	@Override
	public String getDIOType(Resource resource) {
		if (resource == null) {
			return "";
		}

		ValueMap properties = resource.getValueMap();

		return properties.get(Constants.PROP_DIO_TYPE, "");
	}

	@Override
	public boolean isFolder(Resource resource) throws Exception {
		if (resource == null) {
			return false;
		}

		Node node = resource.adaptTo(Node.class);

		return (node.isNodeType("sling:Folder") || node.isNodeType("sling:OrderedFolder"));
	}

	@Override
	public boolean isDamAsset(Resource resource) throws Exception {
		if (resource == null) {
			return false;
		}

		Node node = resource.adaptTo(Node.class);

		return node.isNodeType("dam:Asset");
	}

	@Override
	public boolean isValidImage(Asset asset) {
		return true;
		/*
		 * boolean isValid = false;
		 * 
		 * if(asset == null){ return isValid; }
		 * 
		 * try{ Long size =
		 * NumberUtils.createLong(asset.getMetadataValue("dam:size"));
		 * 
		 * isValid = size > 100; }catch(Exception e){}
		 * 
		 * return isValid;
		 */
	}

	public boolean isWorkflowRunningOnPayload(ResourceResolver resolver, String resourcePath, String workflowPath)
			throws Exception {
		boolean isRunning = false;

		Resource resource = resolver.getResource(resourcePath);

		WorkflowStatus wStatus = resource.adaptTo(WorkflowStatus.class);

		List<Workflow> workflows = wStatus.getWorkflows(false);
		String id = null;

		for (Workflow w : workflows) {
			id = w.getWorkflowModel().getId();

			if (!id.equals(workflowPath)) {
				continue;
			}

			isRunning = true;

			break;
		}

		return isRunning;
	}

	public String getEtcStylePath(String articleStylePath, String stylePathPrefix) {
		String etcStylePath = "";

		if (StringUtils.isEmpty(articleStylePath)) {
			return etcStylePath;
		}

		if (StringUtils.isEmpty(stylePathPrefix)) {
			stylePathPrefix = Constants.STYLE_LOCATION;
		}

		String styleNumber = articleStylePath.substring(articleStylePath.lastIndexOf("/") + 1);

		return stylePathPrefix + "/" + styleNumber.substring(0, 3) + "/" + styleNumber.substring(0, 5) + "/"
				+ styleNumber;
	}

	public String getCollectionPathForArticle(String path) {
		if (StringUtils.isEmpty(path) || StringUtils.containsNone(path, "/")) {
			return path;
		}

		return path.substring(0, path.lastIndexOf("/"));
	}

	public String getColorwayNumber(Resource colorway) {
		if (colorway == null) {
			return "";
		}

		return colorway.getName().split("-")[1];
	}

	public String getStyleNumberFromB2BPath(Node b2bImage) throws Exception {
		if (b2bImage == null) {
			return "";
		}

		String name = b2bImage.getName();

		if (!StringUtils.contains(name, "-")) {
			return "";
		}

		return name.split("-")[0];
	}
}