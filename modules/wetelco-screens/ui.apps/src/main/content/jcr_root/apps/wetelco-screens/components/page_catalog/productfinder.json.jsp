<%@ page import="com.day.cq.wcm.api.Page" %>
<%@ page import="org.apache.sling.api.resource.Resource" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.apache.sling.commons.json.JSONObject" %>
<%@ page import="org.apache.sling.commons.json.io.JSONWriter" %><%--
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
    //find a product in catalog based on product data
    String productPath = request.getParameter("productPath");
    if ( productPath!=null ) {
        String query = "/jcr:root"+resource.getParent().getPath()+"//element(*,nt:unstructured)[jcr:like(productData,'"+productPath+"')]";
        Iterator<Resource> iter = resourceResolver.findResources(query, "xpath");
        if (iter.hasNext()) {
            Page product = pageManager.getContainingPage(iter.next());
            if ( product != null ) {
                response.setContentType("application/json");
                JSONWriter json = new JSONWriter(response.getWriter());
                json.object().key("path").value(product.getPath()).endObject();
                response.flushBuffer();
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

%>