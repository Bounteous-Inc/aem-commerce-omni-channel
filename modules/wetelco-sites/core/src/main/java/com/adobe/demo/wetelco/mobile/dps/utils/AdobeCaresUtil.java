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

import com.adobe.cq.mobile.dps.DPSArticle;
import com.adobe.cq.mobile.dps.DPSBanner;
import com.adobe.cq.mobile.dps.DPSCollection;
import com.adobe.cq.mobile.dps.DPSEntity;
import com.adobe.cq.mobile.dps.DPSObject;
import com.adobe.cq.mobile.dps.DPSProject;
import com.day.cq.wcm.api.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Utilities
 */
public class AdobeCaresUtil {

    /**
     * Static logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AdobeCaresUtil.class);

    public static final String PN_DPS_RESOURCE_TYPE  = "dps-resourceType";
    public static final String DPS_TYPE_PROJECT     = "dps:Project";
    public static final String DPS_TYPE_ARTICLE     = "dps:Article";
    public static final String DPS_TYPE_BANNER      = "dps:Banner";
    public static final String DPS_TYPE_COLLECTION  = "dps:Collection";

    /**
     * Default constructor
     */
    private AdobeCaresUtil() {
        // no instance
    }

    /**
     * Adapt to the appropriate model object
     *
     * @param page page to adapt
     * @return model object if one matched, null otherwise
     */
    public static DPSObject getDPSObject(Page page) {
        DPSObject dpsObject = null;
        String resourceType = getDPSResourceType(page);
        if(resourceType != null){
            if(resourceType.equals(DPS_TYPE_PROJECT)){
                dpsObject = page.adaptTo(DPSProject.class);
            } else if(resourceType.equals(DPS_TYPE_ARTICLE)){
                dpsObject = page.adaptTo(DPSArticle.class);
            } else if(resourceType.equals(DPS_TYPE_BANNER)){
                dpsObject = page.adaptTo(DPSBanner.class);
            } else if(resourceType.equals(DPS_TYPE_COLLECTION)){
                dpsObject = page.adaptTo(DPSCollection.class);
            }
        }
        return dpsObject;
    }

    /**
     * Return pages model type.
     *
     * @param page page to check
     * @return pages dps-resourceType if set, null otherwise
     */
    public static String getDPSResourceType(Page page) {
        String dpsResourceType = null;
        if(page!=null){
            dpsResourceType = page.getProperties().get(PN_DPS_RESOURCE_TYPE, String.class);
        }
        return dpsResourceType;
    }

    /**
     * Check if entity needs an upload.
     *
     * @param dpsEntity
     * @return true if upload required, false otherwise
     */
    public static boolean needsUpload(DPSEntity dpsEntity){
        if(dpsEntity.getId()!=null){
            Date mod = dpsEntity.getLastCQModified();
            Date lastUploaded = dpsEntity.getLastDPSUpload();
            if(mod!=null && lastUploaded!=null){
                return mod.compareTo(lastUploaded) > 0;
            }
        }
        return true;
    }
}
