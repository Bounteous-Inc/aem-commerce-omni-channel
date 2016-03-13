<%@ page import="org.apache.sling.api.resource.Resource" %>
<%@ page import="com.adobe.cq.commerce.api.Product" %>
<%@ page import="com.day.cq.commons.ImageResource" %>
<%@ page import="java.util.Iterator" %>
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
    String[] variantAxes = null;

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
        if (img != null) {
            imageHref = img.getHref();
        } else {
            imageHref = product.getThumbnailUrl();
        }
        variantAxes = product.getProperty("cq:productVariantAxes", String[].class);
    }

%><!DOCTYPE html>
<html>
<head>
    <cq:includeClientLib css="we-retail.demo"/>
</head>
<body>
    <div class="wr-ProductPreview">
        <div class="wr-ColumnLayout">
            <div class="wr-ProductPreview-image"><img src="<%= imageHref %>"/></div>
            <h1 class="wr-ProductPreview-title"><%= title %></h1>
            <h3 class="wr-ProductPreview-price">$<%= price %></h3>
        </div>
        </div>
    </div>
    <cq:includeClientLib js="we-retail.demo"/>
</body>
</html>
