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
package com.adobe.demo.wetelco.mobile.dps.servlets;

import com.adobe.cq.mobile.dps.DPSProject;
import com.day.cq.wcm.api.Page;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.AEMMobileClient;
import com.adobe.demo.wetelco.mobile.dps.services.Importer;
import com.adobe.demo.wetelco.mobile.dps.utils.AdobeCaresUtil;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.engine.SlingRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.rmi.ServerException;

/**
 * POST Handler to import product content from a CSV and generate respective AEM Mobile Project content
 */
@SlingServlet(paths = "/bin/upfile", methods = "POST", metatype = true)
public class ImportFromCSV extends org.apache.sling.api.servlets.SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportFromCSV.class);

    @Reference
    private SlingRequestProcessor slingRequestProcessor=null;

    @Reference
    AdapterManager adapterManager=null;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServerException, IOException {
        try {
            boolean fileReceived = false;
            PrintWriter out = response.getWriter();
            final boolean isMultipart = org.apache.commons.fileupload.servlet.ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                final java.util.Map<String, org.apache.sling.api.request.RequestParameter[]> params = request.getRequestParameterMap();
                for (final java.util.Map.Entry<String, org.apache.sling.api.request.RequestParameter[]> pairs : params.entrySet()) {
                    final String paramName = pairs.getKey();
                    final org.apache.sling.api.request.RequestParameter[] pArr = pairs.getValue();
                    final org.apache.sling.api.request.RequestParameter param = pArr[0];
                    final String filename = param.getFileName();

                    if (paramName.equals("articledata") && filename != null && !filename.isEmpty()) {
                        InputStream stream = null;
                        try {
                            fileReceived = true;
                            Resource resource = request.getResourceResolver().getResource("/content/mobileapps/adobe-cares");
                            Page page = resource.adaptTo(Page.class);
                            DPSProject dpsProject = (DPSProject)AdobeCaresUtil.getDPSObject(page);

                            // Import
                            stream = param.getInputStream();
                            AEMMobileClient aemMobileClient = new AEMMobileClient(request.getResourceResolver(), slingRequestProcessor, adapterManager);
                            Importer importer = new Importer(dpsProject, aemMobileClient);
                            int count = importer.importCSVStream(stream);

                            // Upload


                            // Done
                            out.println("Medical data from the Excel Spread Sheet has been successfully imported into the AEM JCR. Processed " + count + " entries.");
                        } catch (Exception ex) {
                            out.println("Medical data could not be imported into the AEM JCR");
                            throw ex;
                        } finally {
                            if(stream!=null){
                                stream.close();
                            }
                        }
                    }
                }

                if (!fileReceived) {
                    out.println("Required file not provided for import");
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to import data", ex);
        }
    }
}