<%@ page import="com.day.cq.wcm.api.Page" %>
<%@ page import="com.day.cq.wcm.api.PageFilter" %>
<%@ page import="com.day.cq.wcm.foundation.Image" %>
<%@ page import="org.apache.sling.api.resource.SyntheticResource" %>
<%@ page import="org.apache.sling.api.resource.ValueMap" %>
<%@ page import="org.apache.sling.api.wrappers.ValueMapDecorator" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %><%--
  ADOBE CONFIDENTIAL
  __________________

   Copyright 2016 Adobe Systems Incorporated
   All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.
--%><%@include file="/libs/foundation/global.jsp"%><%

    boolean isBackNavigation = resource.isResourceType("wetelco-screens/components/page_product");

    final Page parentPage = currentPage.getParent();
    boolean isTopLevel = resource.isResourceType("wetelco-screens/components/page_catalog")
        || resource.isResourceType("wetelco-screens/components/lookbook");

    boolean isFirstLevel = parentPage.getContentResource().isResourceType("wetelco-screens/components/page_catalog")
        || parentPage.getContentResource().isResourceType("wetelco-screens/components/lookbook");

    Page mainPage = isTopLevel ? currentPage : parentPage;
    String mainPageTitle = mainPage.getPageTitle();
    if (mainPageTitle == null) {
        mainPageTitle = mainPage.getTitle();
    }

    ValueMap mainPageProperties = mainPage.getContentResource().adaptTo(ValueMap.class);
    String[] topNavigationPages = mainPageProperties.get("navigationPages", String[].class);
    if (topNavigationPages == null) {
        mainPageProperties = mainPage.getParent().getContentResource().adaptTo(ValueMap.class);
        topNavigationPages = mainPageProperties.get("navigationPages", String[].class);
    }

    final Iterator<Page> secondaryPages = mainPage.listChildren();

%>
<div class="wr-Menu">
    <% if (isBackNavigation) { %>
    <a class="wr-Menu-button no-pjax" href="<%= xssAPI.getValidHref(parentPage.getPath()) %>.html"></a>
    <% } else { %>
    <div class="wr-Menu-button"></div>
    <% } %>
    <div class="wr-Menu-title">we<span>Telco</span></div>
    <% if (!isBackNavigation) { %>
    <div class="wr-Menu-panel">
        <div class="wr-Navigation">
            <% if (!isTopLevel && !isFirstLevel) { %>
            <a class="wr-Navigation-back" href="<%= xssAPI.getValidHref(mainPage.getPath()) %>.html"></a>
            <a class="wr-Navigation-title" href="<%= xssAPI.getValidHref(mainPage.getPath()) %>.html"><%= xssAPI.encodeForHTMLAttr(mainPageTitle) %></a>
            <% } else { %>
            <div class="wr-Navigation-title" href="<%= xssAPI.getValidHref(mainPage.getPath()) %>.html"><%= xssAPI.encodeForHTMLAttr(mainPageTitle) %></div>
            <% } %>
            <%--sling:include resource="<%= res %>" resourceType="wetelco-screens/components/carousel" /--%>
            <div class="wr-Navigation-list">
                <% if (secondaryPages != null) { %>
                <% while (secondaryPages.hasNext()) {
                    Page secondaryPage = secondaryPages.next();
                    String imagePath = secondaryPage.getContentResource().adaptTo(ValueMap.class).get("coverImage", String.class);
                    Resource imageResource = null;
                    Image image = null;
                    if (imagePath == null) {
                        imageResource = secondaryPage.getContentResource("image");
                        if (imageResource != null) {
                            image = new Image(imageResource);
                            imagePath = secondaryPage.getPath() + ".img.png" + image.getSuffix();
                        }
                    } %>
                <div class="wr-Navigation-item u-showAfterLoad">
                    <span class="wr-Navigation-link" href="<%= xssAPI.getValidHref(secondaryPage.getPath()) %>.html">
                        <div class="wr-Navigation-image" style="background-image: url(<%= xssAPI.getValidHref(imagePath) %>)"></div>
                        <span class="wr-Navigation-label"><%= xssAPI.encodeForHTMLAttr(secondaryPage.getTitle()) %></span>
                    </span>
                </div>
                <% } %>
                <% } %>
            </div>
        </div>

    <%--

        <% if ((isTopLevel || isFirstLevel) && topNavigationPages != null) {
            Resource navigationPageResource;
            Page navigationPage, childPage;
            String navigationPageTitle;
            for (int i = 0; i < topNavigationPages.length; i++) {
                navigationPageResource = resourceResolver.resolve(topNavigationPages[i]);
                navigationPage = navigationPageResource != null ? navigationPageResource.adaptTo(Page.class) : null;
                navigationPageTitle = navigationPage.getPageTitle();
                if (navigationPageTitle == null) {
                    navigationPageTitle = navigationPage.getTitle();
                }
                childPage = navigationPage.listChildren().next();
                if (childPage == null) {
                    childPage = navigationPage;
                }%>
                <a class="wr-Menu-link" href="<%= xssAPI.getValidHref(childPage.getPath()) %>.html"><%= xssAPI.encodeForHTMLAttr(navigationPageTitle) %></a>
            <% }
        } %>
--%>

    </div>
    <% } %>
</div>
