<%--
  Copyright 1997-2011 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================

  Add to cart component

  Adds a product in the given quantity to the cart.

  ==============================================================================

--%><%@ include file="/libs/foundation/global.jsp" %><%
%><%@page session="false" import="
    java.util.Map,
    java.util.HashMap,
    com.adobe.cq.commerce.api.CommerceService,
    com.adobe.cq.commerce.api.CommerceSession, 
    com.adobe.cq.commerce.api.Product,
    org.apache.commons.lang.StringUtils,
    org.apache.sling.api.resource.ResourceResolver,
    org.apache.sling.auth.core.AuthUtil" %>
<%

    ResourceResolver resolver = resource.getResourceResolver();

    String productPath = request.getParameter("product-path");
    String qty = request.getParameter("product-quantity");
    String redirect = request.getParameter("redirect");
    String productNotFound = request.getParameter("redirect-product-not-found");

    String productWrapping = request.getParameter("product-wrapping");
    String productLabel = request.getParameter("product-label");
    boolean wrapping = StringUtils.isNotBlank(productWrapping) &&
            ("on".equals(productWrapping.toLowerCase()) || Boolean.parseBoolean(productWrapping));

    try {
        Resource productResource = resolver.getResource(productPath);

        // Make sure commerceService is adapted from a product resource so that we get
        // the right service implementation (hybris, Geo, etc.)
        CommerceService commerceService = productResource.adaptTo(CommerceService.class);
        CommerceSession session = commerceService.login(slingRequest, slingResponse);

        Product product = productResource.adaptTo(Product.class);

        int quantity = 1;
        if (qty != null && qty.length() > 0) {
            quantity = xssAPI.getValidInteger(qty, 1);
            if (quantity < 0) {
                quantity = 1;
            }
        }

        if (wrapping) {
            Map<String, Object> props = new HashMap<String, Object>();
            props.put("wrapping-selected", Boolean.TRUE);
            if (StringUtils.isNotBlank(productLabel)) {
                props.put("wrapping-label", productLabel);
            }
            session.addCartEntry(product, quantity, props);
        } else {
            session.addCartEntry(product, quantity);
        }

        if (AuthUtil.isRedirectValid(request, redirect)) {
            response.sendRedirect(redirect);
        } else {
            response.sendError(403);
        }
    } catch (Exception e) {
        if (AuthUtil.isRedirectValid(request, productNotFound)) {
            response.sendRedirect(productNotFound);
        } else {
            response.sendError(403);
        }
    }

%>
