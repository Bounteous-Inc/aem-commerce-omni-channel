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

import com.adobe.cq.mobile.dps.DPSObject;
import com.adobe.cq.mobile.dps.DPSProject;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.AEMMobileClient;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.RequestException;
import com.adobe.demo.wetelco.mobile.dps.services.Uploader;
import com.adobe.demo.wetelco.mobile.dps.utils.AEMMoDUtil;
import com.day.cq.wcm.api.Page;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.engine.SlingRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

import java.io.IOException;

/**
 * POST Handler to upload content from an AEM Mobile Project to AEM Mobile On-Demand Services
 */
@Property(name = "service.description", value = "Upload content")
@Component(metatype = false)
@SlingServlet(
        methods = {"POST"},
        resourceTypes = {"cq:Page"},
        selectors = {"adobecares.upload"},
        extensions = {"json"},
        generateComponent = false
)
public class UploadContent extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadContent.class);

    @Reference
    private SlingRequestProcessor slingRequestProcessor;

    @Reference
    private AdapterManager adapterManager;

    /**
     * {@inheritDoc}
     */
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            Resource resource = request.getResource();
            Page page = resource.adaptTo(Page.class);
            JSONObject jsonResponse = new JSONObject();

            try {
                // PARAMS

                // ACTIONS
                JSONObject apiResponseObject = null;
                DPSObject dpsObject = AEMMoDUtil.getDPSObject(page);
                int uploaded = 0;

                if (dpsObject instanceof DPSProject) {
                    AEMMobileClient aemMobileClient = new AEMMobileClient(request.getResourceResolver(), slingRequestProcessor, adapterManager);
                    Uploader uploader = new Uploader((DPSProject)dpsObject, aemMobileClient);
                    uploaded = uploader.upload();
                } else {
                    throw new Exception("Invalid project resource " + page.getPath());
                }

                jsonResponse.put("_elapsed", System.currentTimeMillis() - start);
                jsonResponse.put("uploaded", uploaded);
                jsonResponse.put("response", page.getPath());
            } catch (RequestException ex) {
                LOGGER.error(ex.getDetailedMessage(), ex);
                throw ex;
            }

            // Save and reply
            response.getWriter().print(jsonResponse.toString());
        } catch (Exception ex) {
            LOGGER.error("Uploading of content failed", ex);
            throw new ServletException(ex);
        }
    }
}
