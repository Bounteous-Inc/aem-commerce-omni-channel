/**
 * 
 */
package com.adobe.demo.wetelco.mobile.dps.services;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import com.adobe.demo.wetelco.mobile.dps.mobileclient.RequestException;
import com.day.cq.wcm.api.Page;

/**
 * @author vvenkata
 *
 */
public interface AEMMoDService {

	public Page createCollection(Page sectionPage) throws Exception;

	public Page createArticle(Page productPage) throws Exception;

	public void addBanner(Page collectionPage) throws Exception;

	public void upload(Page dpsPage, String parentCollectionName);

	public void importOnDemandContent(Page catalog)
			throws ServletException, IOException, RequestException, RepositoryException;
}
