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
package com.adobe.demo.wetelco.mobile.dps.utils;

import com.adobe.cq.mobile.dps.DPSException;
import com.adobe.cq.mobile.dps.DPSProject;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.AEMMobileClient;

import org.apache.sling.commons.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.Calendar;

/**
 * Utilities
 */
public class ContentCreationUtil {

	/**
	 * Static logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ContentCreationUtil.class);

	public static final String PARENT_COLLECTION_NAME = "productguide-aem";
	private static String []layout = null;
    
	/**
	 * Default constructor
	 */
	private ContentCreationUtil() {
		// no instance
	}

	public static void updateCollection(Node collectionContentNode,
			String collectionTitle, String imagePath, String bgImagePath,
			String[] layout) throws RepositoryException, JSONException,
			DPSException {
		collectionContentNode.setProperty("cq:lastModified",
				Calendar.getInstance());
		collectionContentNode.setProperty("cq:template",
				"/libs/mobileapps/dps/templates/collection/default");
		collectionContentNode.setProperty("dps-allowDownload",
				new Boolean(true));
		collectionContentNode.setProperty("dps-importance", "normal");

		String productId = "com.org.collection.wetelco."
				+ collectionTitle.replace("-", "");
		if (!collectionTitle.equals(PARENT_COLLECTION_NAME)) {
			collectionContentNode.setProperty("collectionCat",
					PARENT_COLLECTION_NAME);
		} else {
			productId = "com.org.collection.wetelco.";
		}

		String internalKeyword = "subcollection,launch";
		String[] internalKeywords = internalKeyword.split(",");
		collectionContentNode.setProperty("dps-internalKeywords",
				internalKeywords);
		collectionContentNode.setProperty("dps-layoutTitle", layout[0]);
		collectionContentNode.setProperty("dps-layout", layout[1]);
		collectionContentNode.setProperty("dps-readingPosition", "retain");
		collectionContentNode.setProperty("dps-horizontalSwipe", new Boolean(
				true));
		collectionContentNode.setProperty("dps-productId", productId);
		collectionContentNode.setProperty("dps-resourceType", "dps:Collection");
		collectionContentNode.setProperty("dps-shortTitle", collectionTitle);
		collectionContentNode.setProperty("dps-title", collectionTitle);
		collectionContentNode.setProperty("jcr:title", collectionTitle);
		String[] pgetype = { "app-content" };
		collectionContentNode.setProperty("pge-type", pgetype);
		collectionContentNode.setProperty("sling:resourceType",
				"mobileapps/dps/components/page/collection/collection");

		addImage(collectionContentNode, "image", imagePath);
		addImage(collectionContentNode, "background-image", bgImagePath);

		collectionContentNode.getSession().save();
	}

	public static void updateArticle(Node articleContentNode, String md5,
			String articleShortTitle, String collectionTitle,
			String internalKeyword, String articleTitle, String description,
			String price, String feature1, String feature2, String feature3,
			String imagePath) throws Exception {
		if (articleTitle == null || articleTitle.isEmpty()) {
			articleTitle = articleShortTitle;
		}

		articleContentNode.setProperty("cq:lastModified",
				Calendar.getInstance());
		String[] deviceGroups = { "/etc/mobile/groups/responsive" };
		articleContentNode.setProperty("cq:deviceGroups", deviceGroups);

		articleContentNode.setProperty("jcr:title", articleShortTitle);

		String[] pgetype = { "app-content" };
		articleContentNode.setProperty("pge-type", pgetype);

		articleContentNode.setProperty("dps-title", articleTitle);
		String[] internalKeywords = internalKeyword.split(",");
		articleContentNode
				.setProperty("dps-internalKeywords", internalKeywords);
		articleContentNode.setProperty("dps-shortTitle", price);
		articleContentNode.setProperty("dps-access", "free");
		articleContentNode.setProperty("dps-resourceType", "dps:Article");

		articleContentNode.setProperty("importMD5", md5);
		articleContentNode.setProperty("navTitle", articleTitle);
		articleContentNode.setProperty("articleShortTitle", articleShortTitle);
		articleContentNode.setProperty("collectionTitle", collectionTitle);
		articleContentNode.setProperty("internalKeyword", internalKeyword);
		articleContentNode.setProperty("articleTitle", articleTitle);
		articleContentNode.setProperty("description", description);
		articleContentNode.setProperty("price", price);
		articleContentNode.setProperty("feature1", feature1);
		articleContentNode.setProperty("feature2", feature2);
		articleContentNode.setProperty("feature3", feature3);
		articleContentNode.setProperty("collectionCat", collectionTitle);
		articleContentNode.setProperty("imageDirectory", imagePath);

		addImage(articleContentNode, "image", imagePath);
		addImage(articleContentNode, "social-share-image", imagePath);

		articleContentNode.getSession().save();
	}

	public static void updateBanner(Node bannerContentNode, String md5,
			String title, String collectionTitle, String imagePath)
			throws Exception {
		bannerContentNode
				.setProperty("cq:lastModified", Calendar.getInstance());
		bannerContentNode.setProperty("jcr:title", title);

		String[] pgetype = { "app-content" };
		bannerContentNode.setProperty("pge-type", pgetype);

		bannerContentNode.setProperty("dps-title", title);
		bannerContentNode.setProperty("dps-access", "free");
		bannerContentNode.setProperty("dps-resourceType", "dps:Banner");

		bannerContentNode.setProperty("importMD5", md5);
		bannerContentNode.setProperty("collectionCat", collectionTitle);

		addImage(bannerContentNode, "image", imagePath);

		bannerContentNode.getSession().save();
	}

	private static void addImage(Node node, String imageNodeName,
			String imagePath) throws RepositoryException {
		Node image = node.addNode(imageNodeName, "nt:unstructured");
		image.setProperty("fileReference", imagePath);
		image.setProperty("sling:resourceType", "foundation/components/image");
	}

	public static String[] getLayout(AEMMobileClient aemMobileClient,
			DPSProject dpsProject) throws JSONException, DPSException {
		if(layout != null && layout.length == 2 && 
				layout[0].equals("4-Col")) {
			return layout;
		} 
		
		String layoutTitle = "4-Col";
		String layoutURI = aemMobileClient
				.getLayoutURI(dpsProject, layoutTitle);
		if (layoutURI == null) {
			layoutTitle = "Default Layout";
			layoutURI = aemMobileClient.getLayoutURI(dpsProject, layoutTitle);
		}
		layout = new String[] { layoutTitle, layoutURI };
		
		return layout;
	}

}
