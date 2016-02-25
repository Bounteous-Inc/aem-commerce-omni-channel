<%--
  ADOBE CONFIDENTIAL
  __________________

   Copyright 2014 Adobe Systems Incorporated
   All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.
--%>
<%@ page session="false"
         import="   javax.jcr.Node,
                    com.adobe.cq.commerce.api.Product,
                    org.apache.sling.api.resource.Resource,
                    javax.jcr.RepositoryException,
                    com.day.cq.commons.jcr.JcrConstants,
                    javax.jcr.ItemNotFoundException,
                    org.apache.sling.api.SlingHttpServletRequest,
                    org.apache.sling.api.SlingHttpServletResponse,
                    com.adobe.cq.commerce.api.CommerceException,
                    com.adobe.cq.commerce.api.CommerceSession,
                    com.adobe.cq.commerce.api.CommerceService"
%><%

%><%!

    public static final String ATTR_PRODUCT_MASTER = "cq:productMaster";

    /**
     * Walks up the JCR from the given resource to find a page the defines a cq:productMaster reference,
     * and returns the Product that this defines
     * @param res the resource to walk from
     * @return the Product referenced
     * @throws javax.jcr.RepositoryException
     */
    public static Product getProduct(Resource res) throws RepositoryException {
        String productPath = getProductPath(res.adaptTo(Node.class));
        Resource productResource = res.getResourceResolver().resolve(productPath);
        return productResource.adaptTo(Product.class);
    }

    /**
     * Finds the cq:productMaster reference of a page on or above the given node
     * @param node the node to search from
     * @return the value of cq:productMaster
     * @throws RepositoryException
     */
    public static String getProductPath(Node node) throws RepositoryException{
        do{
            if(node.hasNode(JcrConstants.JCR_CONTENT)){
                Node jcrContent = node.getNode(JcrConstants.JCR_CONTENT);
                if(jcrContent.hasProperty(ATTR_PRODUCT_MASTER)){
                    return jcrContent.getProperty(ATTR_PRODUCT_MASTER).getString();
                }
            }
            try {
                node = node.getParent();
            } catch (ItemNotFoundException e) {
                node = null;
            }
        } while(node != null);
        return null;
    }

    /**
     * Gets the properly formatted String representation of the given product's price
     * @param product the product whose price is desired
     * @param currentResource the current context from which the session should be based
     * @param request the current request to authenticate with
     * @param response the current response to authenticate with
     * @return A well-formatted and localized String representation of the product's price
     * @throws com.adobe.cq.commerce.api.CommerceException
     */
    public static String getProductPrice(Product product, Resource currentResource, SlingHttpServletRequest request, SlingHttpServletResponse response) throws CommerceException {
        CommerceSession session = getCommerceSession(currentResource, request, response);
        String result = session.getProductPrice(product);
        session.logout();
        return result;
    }

    /**
     * Gets the current CommerceSession
     * @param currentResource the current context from which the session should be based
     * @param request the current request to authenticate with
     * @param response the current response to authenticate with
     * @return the current CommerceSession
     * @throws CommerceException
     */
    public static CommerceSession getCommerceSession(Resource currentResource, SlingHttpServletRequest request, SlingHttpServletResponse response) throws CommerceException {
        CommerceService commerceService = currentResource.adaptTo(CommerceService.class);
        return commerceService.login(request, response);
    }


%>
