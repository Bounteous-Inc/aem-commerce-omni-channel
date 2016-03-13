/*
 * ADOBE CONFIDENTIAL
 *
 * Copyright 2014 Adobe Systems Incorporated
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

"use strict";

/**
 * Text and Image component JS Use-api script
 */
use(["/libs/wcm/foundation/components/utils/AuthoringUtils.js",
     "/libs/sightly/js/3rd-party/q.js"], function (AuthoringUtils, Q) {
    
    var textimage = {};
    
    var CONST = {
        PROP_ALIGNMENT: "alignment",
        PROP_TEXT: "text"
    };
    
    // The container CSS class name is what defines the alignment
    textimage.alignment = granite.resource.properties[CONST.PROP_ALIGNMENT]
            || currentStyle.get(CONST.PROP_ALIGNMENT, "");
    
    var hasContentDeferred = Q.defer();
    if (granite.resource.properties[CONST.PROP_TEXT]) {
        hasContentDeferred.resolve(true);
    }
    granite.resource.resolve(granite.resource.path + "/image").then(function (imageResource) {
        if (imageResource.properties["fileReference"]) {
            hasContentDeferred.resolve(true);
        } else {
            granite.resource.resolve(granite.resource.path + "/image/file").then(function (localImage) {
                hasContentDeferred.resolve(true);
            }, function () {
                hasContentDeferred.resolve(false);
            });
        }
    }, function () {
        hasContentDeferred.resolve(false);
    });
    
    // TODO: change currentStyle to wcm.currentStyle
    // Adding the constants to the exposed API
    textimage.CONST = CONST;
    
    textimage.isTouch = AuthoringUtils.isTouch;

    textimage.hasContent = hasContentDeferred.promise;

    textimage.news = properties.news;


    return textimage;
    
});