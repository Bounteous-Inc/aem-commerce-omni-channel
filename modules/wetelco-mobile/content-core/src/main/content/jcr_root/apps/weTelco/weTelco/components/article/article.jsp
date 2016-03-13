<%--
	ADOBE CONFIDENTIAL
	__________________

	 Copyright 2014 Adobe Systems Incorporated
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
           import="com.day.cq.i18n.I18n" %>
<%@include file="/libs/foundation/global.jsp" %>
<%@include file="/apps/weTelco/weTelco/global.jsp"%>
<%
	I18n i18n = new I18n(slingRequest);
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
%>
<article class="product-details">
    <div class="product-header">
        <span class="name"><%= xssAPI.encodeForJSString(title) %></span>
        <span class="price"><%= xssAPI.encodeForJSString(productPrice) %></span>
    </div>

    <div class="product-image">
		<cq:include path="article-image" resourceType="foundation/components/image"/>
    </div>

    <div class="product-details-information">
        <h4 class="product-details-description"><%= xssAPI.encodeForJSString(description) %></h4>
        <div class="product-details-summary" ng-bind-html="product.summaryHTML"></div>
    </div>
</article>