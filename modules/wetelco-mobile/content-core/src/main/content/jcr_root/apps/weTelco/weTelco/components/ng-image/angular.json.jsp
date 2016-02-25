<%--
    ADOBE CONFIDENTIAL
    __________________

     Copyright 2013 Adobe Systems Incorporated
     All Rights Reserved.

    NOTICE:  All information contained herein is, and remains
    the property of Adobe Systems Incorporated and its suppliers,
    if any.  The intellectual and technical concepts contained
    herein are proprietary to Adobe Systems Incorporated and its
    suppliers and are protected by trade secret or copyright law.
    Dissemination of this information or reproduction of this material
    is strictly forbidden unless prior written permission is obtained
    from Adobe Systems Incorporated.
--%><%
%><%@ page session="false"
           import="com.day.cq.commons.Doctype,
                   com.day.cq.wcm.foundation.Image,
                   com.day.cq.wcm.foundation.Placeholder,
                   com.day.cq.wcm.api.components.DropTarget,
                   java.util.Map,
                   com.adobe.cq.mobile.angular.data.util.FrameworkContentExporterUtils,
                   org.apache.sling.api.resource.Resource" %><%
%><%@include file="/libs/foundation/global.jsp"%><%
    Image image = new Image(resource);

    //drop target css class = dd prefix + name of the drop target in the edit config
    image.addCssClass(DropTarget.CSS_CLASS_PREFIX + "image");
    image.loadStyleData(currentStyle);
    image.setSelector(".img"); // use image script
    image.setDoctype(Doctype.fromRequest(request));
    // add design information if not default (i.e. for reference paras)
    if (!currentDesign.equals(resourceDesign)) {
        image.setSuffix(currentDesign.getId());
    }
	String linkPath = image.get(Image.PN_LINK_URL);
	boolean hasLink = (linkPath != null && linkPath.length() > 0);
    boolean appExport = Boolean.parseBoolean(slingRequest.getParameter("appExport"));

    // Determine the top level app resource
    Resource topLevelAppResource = FrameworkContentExporterUtils.getTopLevelAppResource(currentPage.adaptTo(Resource.class));
    %>
{
    items: 
	[
        {
            "hasContent": <%= xssAPI.encodeForJSString(String.valueOf(image.hasContent())) %>,
            "imgSrc": "<%= xssAPI.encodeForJSString(
                FrameworkContentExporterUtils.getPathToAsset(topLevelAppResource, image.getSrc(), appExport)) %>",
            "description": "<%= xssAPI.encodeForJSString(image.getDescription()) %>",
            "alt": "<%= xssAPI.encodeForJSString(image.getAlt()) %>",
            "title": "<%= xssAPI.encodeForJSString(image.getTitle()) %>",
            "hasLink": <%= xssAPI.encodeForJSString(String.valueOf(hasLink)) %>,
            "linkPath": "<%= xssAPI.encodeForJSString(linkPath) %>",
        
            "attributes": [
            <%
                Map<String, String> attributesMap = image.getAttributes();
                for (Map.Entry<String, String> currentAttribute : attributesMap.entrySet()) {
            %>
                    { 
                        "attributeName": "<%= xssAPI.encodeForJSString(currentAttribute.getKey()) %>",
                        "attributeValue": "<%= xssAPI.encodeForJSString(currentAttribute.getValue()) %>"
                    },
            <% 
                } 
            %>
            ]
        }
    ]
}