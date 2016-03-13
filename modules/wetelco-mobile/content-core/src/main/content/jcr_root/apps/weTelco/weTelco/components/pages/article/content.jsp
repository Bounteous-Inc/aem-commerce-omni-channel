<%--
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
--%><%@include file="/libs/foundation/global.jsp"%>
<%
    String headerText = currentPage.getTitle();
%><%@page session="false"%>
<div class="bar bar-header bar-telco disable-user-behavior">
	<h1 class="title"><%= xssAPI.encodeForHTML(headerText) %></h1>
</div>

<div class="article-content dps-newcolumn" data-dps-container="flowed"
	style="padding-left: 20px; padding-right: 20px; padding-top: 20px;">
	<cq:include path="content-par"
		resourceType="foundation/components/parsys" />
</div>
