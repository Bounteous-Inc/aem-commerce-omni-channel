/*
 * ADOBE CONFIDENTIAL
 *
 * Copyright 2016 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */
/* globals use, com, org, properties, resource */
use([], function() {
    'use strict';

    var pageResource = resource.resourceResolver.resolve(properties.get('pagePath'));
    if (!pageResource) {
        return null;
    }

    var teaserPage = pageResource.adaptTo(com.day.cq.wcm.api.Page);
    if (!teaserPage) {
        return null;
    }

    var teaserContentResource = teaserPage.getContentResource();
    if (!teaserContentResource) {
        return null;
    }

    var teaserPageProperties = teaserContentResource.adaptTo(org.apache.sling.api.resource.ValueMap);
    if (!teaserPageProperties) {
        return null;
    }

    var parResource = resource.resourceResolver.getResource(teaserContentResource, 'par');
    var parProperties = parResource && parResource.adaptTo(org.apache.sling.api.resource.ValueMap);

    return {
        cssClass: teaserPageProperties.get('class'),
        backgroundImage: parProperties.get('imagePath') ? parProperties.get('imagePath') + '.thumb.319.319.png' :
            properties.get('pagePath') + '.thumb.500.500.png',
        contentPath: parResource.getPath()
    };

});
