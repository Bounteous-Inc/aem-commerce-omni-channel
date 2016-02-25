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

  ----------

  angular-route-fragment.js files _must_ render JS in this form:

      .when('/<path>', {
          templateUrl: '<path to template>',
          controller: '<controller name>'
      })
--%><%
%><%@page session="false"
          import="com.adobe.cq.mobile.angular.data.util.FrameworkContentExporterUtils" %><%
%><%@include file="/libs/foundation/global.jsp" %><%
    String relativeResourcePath = FrameworkContentExporterUtils.getRelativePathToDescendantResource(
            currentPage.adaptTo(Resource.class), resource);
    pageContext.setAttribute("relativeResourcePath", relativeResourcePath);

    slingResponse.setContentType("application/javascript");
%><%
%><c:set var="controllerNameStripped"><%= resource.getPath().replaceAll("[^A-Za-z0-9]", "") %></c:set>
                .when('<c:out value="${resource.path}"/>/:id', {
                    templateUrl: '<c:out value="${relativeResourcePath}"/>.template.html',
                    controller: '<c:out value="${controllerNameStripped}"/>'
                }).when('<c:out value="${resource.path}"/>/:pre1/:pre2/:id', {
                    templateUrl: '<c:out value="${relativeResourcePath}"/>.template.html',
                    controller: '<c:out value="${controllerNameStripped}"/>'
                })
