<%--

  ADOBE CONFIDENTIAL
  __________________

   Copyright 2011 Adobe Systems Incorporated
   All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.

  ==============================================================================

  

  ==============================================================================

--%><%@ include file="/libs/foundation/global.jsp" %><%
%><%@page session="false" import="
    org.apache.sling.auth.core.AuthUtil,
    com.adobe.cq.commerce.api.CommerceService,
    com.adobe.cq.commerce.api.CommerceSession" %><%

    int entryNumber = Integer.parseInt(request.getParameter("entryNumber"));
    int quantity = xssAPI.getValidInteger(request.getParameter("quantity"), -1);
    String redirect = request.getParameter("redirect");
    Boolean wrapping = Boolean.parseBoolean(request.getParameter("wrapping"));
    String label = request.getParameter("label");
    CommerceService commerceService = resource.adaptTo(CommerceService.class);
    CommerceSession session = commerceService.login(slingRequest, slingResponse);

    if (quantity == 0) {
        session.deleteCartEntry(entryNumber);
    } else if (quantity > 0) {
        java.util.Map props = new java.util.HashMap();
        props.put("quantity", quantity);
        props.put("wrapping-selected", wrapping);
        props.put("wrapping-label", label);
        session.modifyCartEntry(entryNumber, props);
    }

    if (AuthUtil.isRedirectValid(request, redirect)) {
        response.sendRedirect(redirect);
    } else {
        response.sendError(403);
    }

%>
