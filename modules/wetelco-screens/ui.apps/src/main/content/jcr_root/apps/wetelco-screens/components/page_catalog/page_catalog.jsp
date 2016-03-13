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
    <% currentDesign.writeCssIncludes(pageContext); %>
    <%@include file="/apps/wetelco-screens/components/common/includecss.jsp"%>
</head>
<body>
<div class="wr-Page wr-CatalogPage">
    <%@include file="/apps/wetelco-screens/components/common/menu.jsp"%>
    <%@include file="/apps/wetelco-screens/components/common/connect_icons.jsp"%>
</div>
<%@include file="/apps/wetelco-screens/components/common/includejs.jsp"%>
</body>
</html>
