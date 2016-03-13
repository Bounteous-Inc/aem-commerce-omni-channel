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
--%><%
%><%@ page session="false"
           import="com.adobe.cq.mobile.angular.data.util.FrameworkContentExporterUtils,
           com.day.cq.wcm.api.Page" %><%
%><%@include file="/libs/foundation/global.jsp" %><%
%><%
    // Controller for this component
    String componentPath = FrameworkContentExporterUtils.getRelativeComponentPath(resource.getPath());
    pageContext.setAttribute("componentPath", componentPath);

    pageContext.setAttribute("componentDataPath", FrameworkContentExporterUtils.getJsFriendlyResourceName(componentPath));

    slingResponse.setContentType("application/javascript");
%>
    /* <c:out value='${resource.name}'/> component controller (path: <c:out value='${componentPath}'/>) */
    data.then(function(response) {
        $scope.<c:out value="${componentDataPath}"/> = response.data["<c:out value='${componentPath}'/>"].items;
    });
    