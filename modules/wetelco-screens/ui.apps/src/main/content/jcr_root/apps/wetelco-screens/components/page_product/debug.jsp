<%@ page import="org.apache.sling.api.resource.Resource" %>
<%@ page import="com.adobe.cq.commerce.api.Product" %>
<%@ page import="com.day.cq.commons.ImageResource" %>
<%--
  ADOBE CONFIDENTIAL
  __________________

   Copyright 2015 Adobe Systems Incorporated
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
%><%@include file="/libs/foundation/global.jsp"%><%
%><%@page session="false" %><%
    String path = "";
    String title = "";
    String imageHref = "";
    String description = "";
    String sku = "";
    String productPath = "";
    String summary = "";
    String features = "";
    double price = 0;
    String[] variantAxes;

    Resource productResource = currentPage.getContentResource("product");
    Product product = productResource == null ? null : productResource.adaptTo(Product.class);

    if (product != null) {
        ImageResource img = product.getImage();

        path = currentPage.getPath();
        title = product.getTitle();
        description = product.getDescription();
        productPath = product.getPIMProduct().getPath();
        summary = product.getProperty("summary", String.class);
        features = product.getProperty("features", String.class);
        price = product.getProperty("price", Double.class);
        sku = product.getSKU();
        imageHref = img.getHref();
        variantAxes = product.getProperty("cq:productVariantAxes", String[].class);
    } else {
        log.error("product problem : " + productResource.getPath());
    }
%><!DOCTYPE html>
<html>
<body>
<h1><%= title %></h1>
<h1><%= path %></h1>
<div><%= description%></div>
<div><%= productPath%></div>
<div><%= title%></div>
<div><%= price %></div>
<div><%= summary%></div>
<img src="<%= imageHref %>"/>
</body>
</html>
