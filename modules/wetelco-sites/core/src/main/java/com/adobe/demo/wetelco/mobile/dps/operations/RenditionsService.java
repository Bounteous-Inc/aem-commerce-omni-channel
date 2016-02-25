package com.adobe.demo.wetelco.mobile.dps.operations;

import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;

import com.day.cq.dam.api.Asset;

/**
 * Created by Daniel on 28/10/15.
 */
public interface RenditionsService {
    Asset generateJpegRenditions(Session session, Resource resource, boolean regenerate)
            throws Exception;

    Asset generateJpegRenditions(Resource resource, boolean regenerate)
            throws Exception;
}
