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
package com.adobe.demo.wetelco.mobile.dps.mobileclient;

import com.adobe.cq.mobile.dps.DPSException;
import com.adobe.cq.mobile.dps.DPSProject;
import com.adobe.cq.mobile.dps.ui.PublishDataSource;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.requestprocessing.FakeRequest;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.requestprocessing.FakeResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.engine.SlingRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client help in making AEM Mobile web requests
 */
public class AEMMobileClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AEMMobileClient.class);

    //OPERATIONS
    public static final String PARAM_OPERATION = ":operation";

    public static final String OPERATION_PREFIX = "dpsapps:";
    public static final String OPERATION_UPLOAD = OPERATION_PREFIX + "dpsUpload";
    public static final String OPERATION_PREVIEW = OPERATION_PREFIX + "preview";
    public static final String OPERATION_IMPORT = OPERATION_PREFIX + "dpsImport";

    // PARAMS
    public static final String PARAM_INCLUDE_CONTENT = "includeContent";
    public static final String PARAM_CREATE_IF_MISSING = "createIfMissing";
    public static final String PARAM_TARGET_COLLECTION = "targetCollection";
    public static final String PARAM_ENTITY_TYPE = "entityType";

    private ResourceResolver resourceResolver = null;
    private SlingRequestProcessor slingRequestProcessor = null;
    private AdapterManager adapterManager = null;

    public AEMMobileClient(ResourceResolver resourceResolver, SlingRequestProcessor slingRequestProcessor, AdapterManager adapterManager){
        this.resourceResolver = resourceResolver;
        this.slingRequestProcessor = slingRequestProcessor;
        this.adapterManager = adapterManager;
    }

    public void performOperation(String path, String operation, NameValuePair[] params) throws RequestException, ServletException, IOException {
        String label = "POST:"+operation + ":"+path;
        LOGGER.info(label);
        OutputStream out = new ByteArrayOutputStream();
        Map<String, Object> mapparams = new HashMap<String, Object>();
        for (int i = 0; i < params.length; i++) {
            NameValuePair nvp = params[i];
            mapparams.put(nvp.getName(), nvp.getValue());
        }
        FakeRequest request1 = new FakeRequest("POST", path, mapparams );
        FakeResponse response = new FakeResponse(out);
        slingRequestProcessor.processRequest(request1, response, resourceResolver);
        response.getWriter().flush();
        String responseAsString = out.toString();

        if (response.getStatusCode() == HttpStatus.SC_OK) {
            LOGGER.trace("DPS operation " + operation + ", path: " + path + ": [success]");
        } else {
            String errorMsg = response.getStatusMsg();
            LOGGER.trace("DPS operation " + operation + ", path: " + path + ", code: " + response.getStatusCode() + ": [error:"+errorMsg+"]");
            LOGGER.warn(label + " >>> RESPONSE >>> "  +responseAsString);
            throw new RequestException(response.getStatusCode(), errorMsg==null? "Failed request":errorMsg);
        }
    }

    public void upload(String path, String targetCollection) throws ServletException, IOException, RequestException {
        LOGGER.info("Upload " + path + (targetCollection==null?"":" with target collection " + targetCollection));
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new NameValuePair(PARAM_OPERATION, OPERATION_UPLOAD));
        list.add(new NameValuePair(PARAM_CREATE_IF_MISSING, "true"));
        list.add(new NameValuePair(PARAM_INCLUDE_CONTENT, "true"));
        if(targetCollection!=null){
            list.add(new NameValuePair(PARAM_TARGET_COLLECTION, targetCollection));
        }
        performOperation(path, OPERATION_UPLOAD, list.toArray(new NameValuePair[list.size()]));
    }

    public void importOnDemandContent(String path, String importType) throws ServletException, IOException, RequestException {
        //curl -u admin:admin -X POST -F ":operation=dpsapps:dpsImport" -F "entityType=ALL" http://localhost:4502/content/mobileapps/adobe-cares
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new NameValuePair(PARAM_OPERATION, OPERATION_IMPORT));
        list.add(new NameValuePair(PARAM_ENTITY_TYPE, importType));
        performOperation(path, OPERATION_IMPORT, list.toArray(new NameValuePair[list.size()]));
    }

    public void preview(String path) throws ServletException, IOException, RequestException {
        // curl -u admin:admin -X POST -F ':operation=dpsapps:preview' http://localhost:4502/content/mobileapps/adobe-cares
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new NameValuePair(PARAM_OPERATION, OPERATION_PREVIEW));
        performOperation(path, OPERATION_PREVIEW, list.toArray(new NameValuePair[list.size()]));
    }

    public String getLayoutURI(DPSProject project, String layoutTitle) throws DPSException, JSONException {
        PublishDataSource publishDataSource = adapterManager.getAdapter(project, PublishDataSource.class);
        JSONObject jsonObject = publishDataSource.getLayouts(null);
        JSONArray layoutArray = jsonObject.getJSONArray("data");
        for (int i = 0; i < layoutArray.length(); i++) {
            JSONObject layout = layoutArray.getJSONObject(i);
            if(layoutTitle.equals(layout.get("title"))){
                return layout.getString("entityURL");
            }
        }
        return null;
    }
}
