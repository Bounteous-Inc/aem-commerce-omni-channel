<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.sling.api.resource.Resource" %>
<%@ page import="com.adobe.cq.commerce.api.Product" %>
<%@ page import="com.day.cq.commons.ImageResource" %>
<%@ page import="com.adobe.cq.commerce.api.CommerceSession" %>
<%@ page import="com.adobe.cq.commerce.api.CommerceService" %>
<%@ page import="com.adobe.cq.commerce.api.smartlist.SmartListManager" %>
<%@ page import="com.adobe.cq.commerce.api.smartlist.SmartList" %>
<%@ page import="com.adobe.cq.commerce.api.smartlist.SmartListEntry" %>
<%@ page import="com.adobe.cq.commerce.common.AxisFilter" %>
<%@ page import="com.adobe.cq.commerce.common.CommerceHelper" %>
<%@ page import="java.lang.NumberFormatException" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="com.adobe.cq.commerce.common.EnumerateAxisFilter" %>
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
    final String PROJECT_SMARTLIST = "we-retails-smartlist";
    final String PRODUCT_SUFFIX = "/jcr:content/product";

    final HashMap<String, Integer> sizesMap = new HashMap<String, Integer>();
    sizesMap.put("xxs", 0);
    sizesMap.put("xs", 1);
    sizesMap.put("s", 2);
    sizesMap.put("m", 3);
    sizesMap.put("l", 4);
    sizesMap.put("xl", 5);
    sizesMap.put("xxl", 6);

    String path = "";
    String title = "";
    String imageHref = "";
    String description = "";
    String sku = "";
    String productPath = "";
    String summary = "";
    String features = "";
    Double price = 0.0;
    boolean isInSmartList = false;
    Map<String, Product> variationsSize = new TreeMap<String, Product>(new Comparator<String>(){
        public int compare(String s1, String s2) {
            Integer i1, i2;
            try {
                i1 = Integer.parseInt(s1);
                i2 = Integer.parseInt(s2);
                return i1.compareTo(i2);
            } catch (NumberFormatException e) {
                i1 = sizesMap.get(s1.toLowerCase());
                i2 = sizesMap.get(s2.toLowerCase());
                if (i1 != null && i2 != null) {
                    return i1.compareTo(i2);
                }
                return s1.compareTo(s2);
            }
        }
    });
    Map<String, Product> variationsColor = new TreeMap<String, Product>();

    Resource productResource = currentPage.getContentResource("product");
    Product product = productResource == null ? null : productResource.adaptTo(Product.class);

    Product pimProduct;
    boolean slDisabled = true;
    if (product != null) {
        ImageResource img = product.getImage();

        path = currentPage.getPath();
        title = product.getTitle();
        description = product.getDescription();
        pimProduct = product.getPIMProduct();
        productPath = pimProduct != null ? pimProduct.getPath() : null;
        summary = product.getProperty("summary", String.class);
        features = product.getProperty("features", String.class);
        price = product.getProperty("price", Double.class);
        sku = product.getSKU();

        if (img != null) {
            imageHref = img.getHref();
        } else {
            imageHref = product.getThumbnailUrl();
        }

        final CommerceService commerceService = resource.adaptTo(CommerceService.class);
        final CommerceSession commerceSession = commerceService.login(slingRequest, slingResponse);

        final SmartListManager smartListManager = commerceSession.getSmartListManager();
        final SmartList list = smartListManager.getSmartList(PROJECT_SMARTLIST);
        if (list != null) {
            slDisabled = false;
            Iterator<SmartListEntry> iter = list.getSmartListEntries();
            while (iter.hasNext()) {
                SmartListEntry sle = iter.next();
                if (sle.getProduct().getPath().equals(path + PRODUCT_SUFFIX)) {
                    isInSmartList = true;
                    break;
                }
            }
        }

        Iterator<Product> unorderedVariations = product.getVariants();
        while (unorderedVariations.hasNext()) {

            Product variation = unorderedVariations.next();
            String color = variation.getProperty("color", String.class);
            if ( color != null) {
                variationsColor.put(color, variation);
            }
            String size = variation.getProperty("size", String.class);
            if ( size != null) {
                variationsSize.put(size, variation);
            }
        }
    }

    NumberFormat formatter = new DecimalFormat("#.00");
    String sPrice = price != null ? formatter.format(price) : null; // Creates a string containing "3.14"

%><!DOCTYPE html>
<html>
<head>
    <% currentDesign.writeCssIncludes(pageContext); %>
    <%@include file="/apps/we-retail-instore/components/common/includecss.jsp"%>
</head>
<body>
    <div class="wr-Page wr-CatalogProductPage">
        <%@include file="/apps/we-retail-instore/components/common/menu.jsp"%>
        <%@include file="/apps/we-retail-instore/components/common/connect_icons.jsp"%>
        <div style="display: table; width: 100%; height: 100%;">
            <div class="wr-ColumnLayout" style="display: table-cell; vertical-align: middle;">
                <div class="wr-ColumnLayout-50" style="display: table; height: 100%;">
                    <div class="wr-ProductDetail-image" style="display: table-cell; vertical-align: middle;">
                        <%--<img src="<%= imageHref != null ? imageHref : "" %>"/>--%>
                            <sling:include resource="<%= product.getAsset() %>"/>
                    </div>
                </div>
                <div class="wr-ColumnLayout-50" style="display: table; height: 100%;">
                    <div style="display: table-cell; vertical-align: middle; padding-right: 170px">
                        <h3 class="wr-ProductDetail-description"><%= description %><span class="wr-ProductDetail-sku">SKU <%= sku %></span></h3>
                        <h1 class="wr-ProductDetail-title"><%= title %></h1>
                        <div class="wr-ProductDetail-summary"><%= summary %><br><%= features %></div>
                        <h4 class="wr-ProductDetail-techSpec">Tech Specs</h4>

                        <div class="wr-ProductDetail-price">$<%= sPrice %>
                            <div class="geo-StarRatingView">
                                <span class="geo-StarRatingView-rate is-filled" data-star="1">
                                    <i class="geo-StarRatingView-rate-frame"></i>
                                    <i class="geo-StarRatingView-rate-fill"></i>
                                </span>
                                <span class="geo-StarRatingView-rate is-filled" data-star="2">
                                    <i class="geo-StarRatingView-rate-frame"></i>
                                    <i class="geo-StarRatingView-rate-fill"></i>
                                </span>
                                <span class="geo-StarRatingView-rate is-filled" data-star="3">
                                    <i class="geo-StarRatingView-rate-frame"></i>
                                    <i class="geo-StarRatingView-rate-fill"></i>
                                </span>
                                <span class="geo-StarRatingView-rate is-filled" data-star="4">
                                    <i class="geo-StarRatingView-rate-frame"></i>
                                    <i class="geo-StarRatingView-rate-fill"></i>
                                </span>
                                <span class="geo-StarRatingView-rate" data-star="5">
                                    <i class="geo-StarRatingView-rate-frame"></i>
                                    <i class="geo-StarRatingView-rate-fill"></i>
                                </span>
                                <span class="geo-StarRatingView-count">13 reviews</span>
                            </div>
                        </div>
                        <div style="border-bottom: 1px solid #ccc"></div>
                        <% if (variationsSize.size() > 0) {  %>
                        <div class="wr-ProductDetail-size">
                            <div class="wr-ProductDetail-sizes">Sizes</div>
                            <%
                                for (String size : variationsSize.keySet()) {
                                    %><div class="wr-ProductDetail-sizeEntry"><%= size %></div><%
                            }
                        %>
                        </div>
                        <% } %>

                        <% if (variationsColor.size() > 0) {  %>
                            <div class="wr-ProductDetail-color">
                                <div class="wr-ProductDetail-colors">Colors</div>
                                <%
                                    for (String color : variationsColor.keySet()) {
                                        Product variant = variationsColor.get(color);
                                        Resource img = variant.getImage();
                                        Resource asset = variant.getAsset();

                                        String targetImg = "";
                                        if (variationsColor.size() > 1) {
                                            if (img != null) {
                                                targetImg = img.getPath();
                                            } else {
                                                if (asset != null) {
                                                    targetImg = asset.getPath();
                                                }
                                            }
                                        }
                                        %><div class="wr-ProductDetail-colorEntry"><img class="js-Color-selector" data-target="<%=targetImg%>" src="<%= variant.getThumbnailUrl() %>"></div><%
                                    }

                                %>
                            </div>
                        <% } %>

                        <div style="border-bottom: 1px solid #ccc; margin-bottom: 24px;"></div>
                        <button <%= slDisabled ? "disabled style=\"background-color: #f0f0f0\"" : "" %> class="wr-ProductDetail-buttonWishlist js-add-product-to-wishlist" data-isinsmartlist="<%=isInSmartList%>" data-path="<%= path %>" data-product-path="<%= productPath %>"><%=!isInSmartList ? "Add to WishList" : "Remove from WishList"%></button>
                        <button class="wr-Button wr-Button--callClerk">Questions? Just ask.</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <%@include file="/apps/we-retail-instore/components/common/includejs.jsp"%>
</body>
</html>
