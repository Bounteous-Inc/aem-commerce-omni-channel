/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// Server-side JavaScript for the head.html logic

"use strict";

var global = this;

use(["/libs/wcm/foundation/components/utils/ResourceUtils.js"], function (ResourceUtils) {
    
    var CONST = {
        PROP_DESIGN_PATH: "cq:designPath"
    };
    
    var _getKeywords = function () {
        var keywords = "";
        if (global.currentPage) {
            var tags = global.currentPage.getTags();
            for (var tagIdx=0 ; tagIdx < tags.length ; tagIdx++) {
                keywords += tags[tagIdx].getTitle();
                keywords += (tagIdx <= tags.length - 1) ? "," : "";
            }
        }
        
        return keywords;
    };
    
    var designPathPromise = ResourceUtils.getContainingPage(granite.resource).then(function (pageResource) {
        return ResourceUtils.getInheritedPageProperty(pageResource, CONST.PROP_DESIGN_PATH)
            .then(function (designPath) {
                return designPath;
            });
    });

    var faviconPathPromise = designPathPromise.then(function (designPath) {
        return designPath + "/favicon.ico";
    });

    var titlePromise = ResourceUtils.getContainingPage(granite.resource).then(function (pageResource) {
        return ResourceUtils.getResource(pageResource.path + "/jcr:content")
            .then(function (contentResource) {
                var title = contentResource.properties["jcr:title"];
                if (!title) {
                    title = pageResource.name;
                }
                
                return title;
            });
    });

      var pagetitlePromise = ResourceUtils.getContainingPage(granite.resource).then(function (pageResource) {
        return ResourceUtils.getResource(pageResource.path + "/jcr:content")
            .then(function (contentResource) {
                var pagetitle = contentResource.properties["pageTitle"];
                if (!pagetitle) {
                    pagetitle = pageResource.name;
                }
                
                return pagetitle;
            });
    });

    var pagePathPromise = ResourceUtils.getContainingPage(granite.resource).then(function (pageResource) {
        return pageResource.path + ".html";
    });
    
    var keywords = _getKeywords();
    
    return {
        keywords: keywords,
        description: granite.resource.properties["jcr:description"],
        faviconPath: faviconPathPromise,
        designPath: designPathPromise,
        pagePath: pagePathPromise,
        title: titlePromise,
        pagetitle: pagetitlePromise
    };
});