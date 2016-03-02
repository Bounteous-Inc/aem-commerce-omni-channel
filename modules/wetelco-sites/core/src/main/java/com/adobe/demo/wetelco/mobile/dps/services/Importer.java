/*************************************************************************
 * ADOBE CONFIDENTIAL
 * __________________
 * <p/>
 * Copyright 2016 Adobe Systems Incorporated
 * All Rights Reserved.
 * <p/>
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

import com.adobe.cq.mobile.dps.DPSException;
import com.adobe.cq.mobile.dps.DPSProject;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.AEMMobileClient;
import com.adobe.demo.wetelco.mobile.dps.utils.ContentCreationUtil;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.sling.commons.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;

/**
 * Import product content from a CSV and generate respective AEM Mobile Project
 * content
 */
public class Importer {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Importer.class);

	private DPSProject dpsProject = null;
	private AEMMobileClient aemMobileClient = null;
	private String[] layout = null;

	public Importer(DPSProject dpsProject, AEMMobileClient aemMobileClient) {
		this.dpsProject = dpsProject;
		this.aemMobileClient = aemMobileClient;
	}

	public int importCSVStream(InputStream is) throws Exception {
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(is);
			int count = 0;
			CSVParser parser = new CSVParser(inputStreamReader, CSVFormat.EXCEL);
			for (CSVRecord csvRecord : parser) {
				if (count++ > 0) {
					importCSVRow(csvRecord);
				}
			}
			return count;
		} catch (Exception ex) {
			throw new Exception("Failed to import product data", ex);
		} finally {
			if (inputStreamReader != null) {
				inputStreamReader.close();
			}
		}
	}

	// Stores medical data in the Adobe CQ JCR
	private void importCSVRow(CSVRecord csvRecord) throws Exception {
		// try {
		// Node appNode = dpsProject.adaptTo(Node.class);
		// Session session = appNode.getSession();
		//
		// String shortTitle = getString(csvRecord.get(0));
		// String collectionTitle = getString(csvRecord.get(1));
		// String internalKeyword = getString(csvRecord.get(2));
		// String title = getString(csvRecord.get(3));
		// String description = getString(csvRecord.get(4));
		// String price = getString(csvRecord.get(5));
		// String feature1 = getString(csvRecord.get(6));
		// String feature2 = getString(csvRecord.get(7));
		// String feature3 = getString(csvRecord.get(8));
		// String imagePath = "/content/dam/adobe-cares" +
		// getString(csvRecord.get(9));
		// if (csvRecord.size() != 10) {
		// throw new Exception("Failed to parse row " + csvRecord.toString());
		// }
		//
		// // COLLECTION
		// Node collectionNode = null;
		// String collectionPath = appNode.getPath() + "/collections/" +
		// collectionTitle;
		// if (!session.nodeExists(collectionPath)) {
		// Node collectionsParentNode =
		// getFolderNodeForGeneratedContent(appNode, "collections");
		// collectionNode = collectionsParentNode.addNode(collectionTitle,
		// "cq:Page");
		// LOGGER.info("CREATED COLLECTION: " + collectionTitle);
		// Node collectionContentNode = collectionNode.addNode("jcr:content",
		// "cq:PageContent");
		// //ContentCreationUtil.updateCollection(collectionContentNode,
		// collectionTitle, imagePath, getLayout());
		// } else {
		// LOGGER.info("SKIPPING COLLECTION UPDATE: " + collectionTitle);
		// }
		//
		// // ENTITY : ARTICLE or BANNER
		// Node entityNode = null;
		// String entityName = collectionTitle + shortTitle;
		// boolean isBanner = "banner".equalsIgnoreCase(internalKeyword);
		// String parentNodeName = isBanner ? "banners" : "articles";
		// String entityPath = appNode.getPath() + "/" + parentNodeName + "/" +
		// entityName;
		// String md5 = Base64.encodeBase64String(DigestUtils.md5(new
		// StringBufferInputStream(csvRecord.toString())));
		//
		// if (!session.nodeExists(entityPath)) {
		// Node entityParentNode = getFolderNodeForGeneratedContent(appNode,
		// parentNodeName);
		// entityNode = entityParentNode.addNode(entityName, "cq:Page");
		// LOGGER.info("CREATED: " + entityPath);
		// Node entityContentNode = entityNode.addNode("jcr:content",
		// "cq:PageContent");
		// if (isBanner) {
		// ContentCreationUtil.updateBanner(entityContentNode, md5, shortTitle,
		// collectionTitle, imagePath);
		// } else {
		// ContentCreationUtil.updateArticle(entityContentNode, md5, shortTitle,
		// collectionTitle, internalKeyword, title, description, price,
		// feature1, feature2, feature3, imagePath);
		// }
		// } else {
		// entityNode = session.getNode(entityPath);
		// Node entityContentNode = entityNode.getNode("jcr:content");
		//
		// if (isUpdated(entityContentNode, md5)) {
		// resetNode(entityContentNode);
		// LOGGER.info("UPDATED : " + entityPath);
		// if (isBanner) {
		// ContentCreationUtil.updateBanner(entityContentNode, md5, shortTitle,
		// collectionTitle, imagePath);
		// } else {
		// ContentCreationUtil.updateArticle(entityContentNode, md5, shortTitle,
		// collectionTitle, internalKeyword, title, description, price,
		// feature1, feature2, feature3, imagePath);
		// }
		// } else {
		// LOGGER.info("SKIPPING UPDATE: " + entityPath);
		// }
		// }
		// } catch (Exception ex) {
		// throw new Exception("Failed to import data", ex);
		// }
	}

	private Node getFolderNodeForGeneratedContent(Node appNode,
			String entityFolderName) throws RepositoryException {
		Node entityFolderNode = appNode.getNode(entityFolderName);
		return entityFolderNode;
		/**
		 * Don't use folders yet if(entityFolderNode.hasNode(NN_GENERATED)){
		 * return entityFolderNode.getNode(NN_GENERATED); } else { return
		 * entityFolderNode.addNode(NN_GENERATED, "sling:Folder"); }
		 */
	}

	private void resetNode(Node node) throws RepositoryException {
		PropertyIterator propertyIterator = node.getProperties();
		while (propertyIterator.hasNext()) {
			Property property = propertyIterator.nextProperty();
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

	private String getString(String cellData) {
		return cellData.trim().isEmpty() ? null : cellData;
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

	private String[] getLayout() throws JSONException, DPSException {
		if (layout == null) {
			layout = ContentCreationUtil.getLayout(aemMobileClient, dpsProject);
		}
		return layout;
	}
}
