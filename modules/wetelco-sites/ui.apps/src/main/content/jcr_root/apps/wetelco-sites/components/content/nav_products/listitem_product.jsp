<%@page session="false"%><%--
  ADOBE CONFIDENTIAL
  __________________

   Copyright 2012 Adobe Systems Incorporated
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
%><%@ include file="/libs/foundation/global.jsp" %><%
%><%@ page contentType="text/html; charset=utf-8" import="
    com.adobe.cq.commerce.api.CommerceSession,
    com.adobe.cq.commerce.common.CommerceHelper,
    com.adobe.cq.commerce.api.Product,
    info.geometrixx.commons.util.GeoHelper,
    com.adobe.cq.commerce.api.CommerceService,
    java.util.Locale"%><%

    final Locale pageLocale = currentPage.getLanguage(false);
    final String language = pageLocale.getLanguage();

    CommerceService commerceService = resource.adaptTo(CommerceService.class);
    CommerceSession session = commerceService.login(slingRequest, slingResponse);

    final Page listItem = (Page)request.getAttribute("listitem");
    final Product product = CommerceHelper.findCurrentProduct(listItem);
    if (product != null) {
        final String pagePath = listItem.getPath();
        final String title = product.getTitle(language);
		final String description = product.getDescription(language);
        final String price = session.getProductPrice(product);

%><div class="col-sm-4 col-lg-4 col-md-4" style="height: 350px; padding-left: 0px; padding-right: 0px; margin-top: 20px;">
	<div class="thumbnail" style="padding: 20px; border-right-width: 1px;">
    <a href="<%= xssAPI.getValidHref(pagePath) %>.html" title="<%= xssAPI.encodeForHTMLAttr(title) %>"
       onclick="CQ_Analytics.record({event: 'listItemClicked', values: { listItemPath: '<%= xssAPI.encodeForJSString(pagePath) %>' }, collect: false, options: { obj: this }})">
        <img src="<%= xssAPI.getValidHref(product.getThumbnailUrl(160)) %>" alt="<%= xssAPI.encodeForHTMLAttr(title) %>"
        style="width: 160px; height: 120px"/>
        <h4><%= xssAPI.encodeForHTML(title) %></h4>
        <p><%= xssAPI.encodeForHTML(description) %></p>   
    </a>
	</div>    
</div><%

    }

%>
