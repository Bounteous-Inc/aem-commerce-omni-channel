<%@ page import="org.apache.sling.api.resource.Resource" %>
<%@ page import="com.adobe.cq.commerce.api.Product" %>
<%@ page import="com.day.cq.commons.ImageResource" %>
<%@ page import="com.day.cq.wcm.api.Page" %>
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
%><%@page session="false" %><!DOCTYPE html>
<html>
<head>
    <%@include file="/apps/wetelco-screens/components/common/includecss.jsp"%>
</head>
<body>
    <%
        Iterator<Page> childrenIterator;

        /*
        This page is used for categories (Equipment) and sub-categories (shoes).
        Categories can show sub-categories or products. For that reason we
        need to first determine if we show sub-categories or products here.
        */
        boolean hasSubCategories = false;

        childrenIterator = currentPage.listChildren();
        while (childrenIterator.hasNext()) {
            Page n = childrenIterator.next();

            Resource sectionResource = n.getContentResource();

            if (sectionResource.isResourceType("wetelco-screens/components/page_section")) {
                hasSubCategories = true;
                break;
            }
        }

        Resource currentResource = currentPage.getContentResource();
        ValueMap currentProperties = currentResource.getValueMap();
        Page categoryPage = hasSubCategories ? currentPage : currentPage.getParent();

        String coverImagePath = currentProperties.get("coverImage", String.class);
        String description = categoryPage.getDescription();
        String titleCssClass = hasSubCategories ? "wr-SubcategoryTitle" : "wr-ProductSectionTitle";

    %>
    <div class="wr-Page <%= hasSubCategories ? "wr-CatalogSectionPage" : "wr-CatalogSubSectionPage" %>">
        <%@include file="/apps/wetelco-screens/components/common/menu.jsp"%>
        <%@include file="/apps/wetelco-screens/components/common/connect_icons.jsp"%>
        <div class="<%= titleCssClass %>" style="background-image: url(<%= coverImagePath %>);">
          <h1 class="wr-ProductSectionTitle-header"><%= currentPage.getTitle() %></h1>
          <div class="wr-ProductSectionTitle-description">
            <%= (description != null) ? description : "" %>
          </div>
        </div>
        <%

        if (hasSubCategories) {

        %>
        <div class="wr-SubcategoryGrid">
            <%
                childrenIterator = currentPage.listChildren();

                while (childrenIterator.hasNext()) {
                    Page n = childrenIterator.next();
                    Resource sectionResource = n.getContentResource();

                    if (sectionResource.getResourceType().equals("wetelco-screens/components/page_section")) {
                    %>
                    <a class="wr-SubcategoryGrid-entry" href="<%= n.getPath() %>.html"><%= n.getTitle() %></a>
                    <%
                    }
                }
            %>
        </div>
        <%

        } else {

        %>
        <div class="wr-ProductGrid">
            <%
                childrenIterator = currentPage.listChildren();

                while (childrenIterator.hasNext()) {
                    Page n = childrenIterator.next();

                    Resource productResource = n.getContentResource("product");
                    Product product = productResource == null ? null : productResource.adaptTo(Product.class);

                    Double price = product != null ? product.getProperty("price", Double.class) : null;
                    String title = product != null ? product.getTitle() : null;

                    if (product != null && title != null) {
                        ImageResource img = product.getImage();
                        String imageHref;
                        if (img != null) {
                            imageHref = img.getHref();
                        } else {
                            imageHref = product.getThumbnailUrl();
                        }
                        %><span href="<%= n.getPath() %>.html" class="wr-ProductGrid-entry u-showAfterLoad">
                            <img src="<%= imageHref %>" class="wr-ProductGrid-entryImage" />
                            <strong class="wr-ProductGrid-entryTitle"><%= title %></strong>
                        </span><%
                    }
                }
            %>
        </div>
        <%

        }

        %>
    </div>
    <%@include file="/apps/wetelco-screens/components/common/includejs.jsp"%>
</body>
</html>
