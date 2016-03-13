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
           import="com.adobe.cq.commerce.api.Product,
                    org.apache.sling.api.resource.Resource" %><%
%><%@include file="/libs/foundation/global.jsp"%><%
        %><%@include file="/apps/weTelco/weTelco/global.jsp"%><%

    // Get the product this page represents
    Resource currentPageResource = currentPage.adaptTo(Resource.class);
    String productPrice = "n/a";
    String summaryHTML = "";
    String title = "";
    String description = "";
    String SKU = "";
    Product product = getProduct(currentPageResource);

    if (product != null) {
        summaryHTML = product.getProperty("summary", String.class);
        if (summaryHTML == null || summaryHTML.equals("...")) {
            summaryHTML = "";
        }
        title = product.getTitle();
        description = product.getDescription();
        SKU = product.getSKU();
        productPrice = getProductPrice(product, currentPageResource, slingRequest, slingResponse);
    }
    request.setAttribute("productPrice", productPrice);

    // TODO: implement numberOfLikes and numberOfComments
%>
{
    items: 
	[
        {
            'name': '<%= xssAPI.encodeForJSString(title) %>',
            'description': '<%= xssAPI.encodeForJSString(description) %>',
            'summaryHTML': '<%= xssAPI.encodeForJSString(summaryHTML) %>',
            'price': '<%= xssAPI.encodeForJSString(productPrice) %>',
            'SKU': '<%= xssAPI.encodeForJSString(SKU) %>',
            'numberOfLikes': '0',
            'numberOfComments': '0'
        }
    ]
}