<%@ page session="false" %>
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
--%>
<%@ include file="/libs/foundation/global.jsp" %>
<%@ page contentType="text/html; charset=utf-8"
         import="java.util.ResourceBundle,
                 java.util.Locale,
                 java.util.List,
                 com.adobe.cq.commerce.api.smartlist.SmartList,
                 com.adobe.cq.commerce.api.smartlist.SmartListManager,
                 org.apache.commons.lang.StringUtils,
                 com.adobe.granite.security.user.UserProperties,
                 com.day.cq.personalization.UserPropertiesUtil,
                 com.adobe.cq.commerce.api.CommerceConstants,
                 com.adobe.cq.commerce.api.CommerceService,
                 com.adobe.cq.commerce.api.CommerceSession,
                 com.adobe.cq.commerce.api.Product,
                 com.adobe.cq.commerce.common.CommerceHelper,
                 com.day.cq.i18n.I18n" %>
<%
%><%
    final Locale pageLocale = currentPage.getLanguage(false);
    final ResourceBundle bundle = slingRequest.getResourceBundle(pageLocale);
    final I18n i18n = new I18n(bundle);
    final String addToSmartListUrl = currentPage.getPath() + ".commerce.smartlist.management.html";
    String smartListRedirect = CommerceHelper.mapPathToCurrentLanguage(currentPage, WCMUtils.getInheritedProperty(currentPage, resourceResolver, "cq:smartListPage"));
    if (StringUtils.isEmpty(smartListRedirect)) {
        smartListRedirect = currentPage.getPath();
    }
    final String productPath = slingRequest.getParameter("productPath");

    CommerceService commerceService = resource.adaptTo(CommerceService.class);
    CommerceSession commerceSession = commerceService.login(slingRequest, slingResponse);

    List<SmartList> smartLists = null;
    final UserProperties userProperties = slingRequest.adaptTo(UserProperties.class);
    if (userProperties != null || !UserPropertiesUtil.isAnonymous(userProperties)) {
        final SmartListManager smartListManager = commerceSession.getSmartListManager();
        smartLists = smartListManager.getSmartLists(null);
    }

    final String smartlistSelectExistingId = xssAPI.encodeForHTMLAttr("cq-addtosmartlist-existing-" + System.currentTimeMillis());
    final String smartlistSelectExistingSelectId = xssAPI.encodeForHTMLAttr("cq-addtosmartlist-existing-select-" + System.currentTimeMillis());
    final String smartlistSelectNewId = xssAPI.encodeForHTMLAttr("cq-addtosmartlist-new-" + System.currentTimeMillis());
    final String smartlistSelectNewTitleId = xssAPI.encodeForHTMLAttr("cq-addtosmartlist-new-title-" + System.currentTimeMillis());
    final String smartlistSelectNewDefaultId = xssAPI.encodeForHTMLAttr("cq-addtosmartlist-new-default-" + System.currentTimeMillis());
%>

<script type="text/javascript">
    $CQ(document).ready(function () {
        $CQ("input[name=smartlist-select][value=existing]").click(function () {
            $CQ(".existing-list").show();
            $CQ(".new-list").hide();

        });
        $CQ("input[name=smartlist-select][value=new]").click(function () {
            $CQ(".existing-list").hide();
            $CQ(".new-list").show();

        });
    });
</script>

<div class="cq-commerce-add-to-smartlist">
    <form method="post" action="<%= xssAPI.getValidHref(addToSmartListUrl) %>">
        <input type="hidden" name="redirect" value="<%= resourceResolver.map(request, smartListRedirect) %>.html">
        <input type="hidden" name=":operation" value="addToSmartList">
        <input type="hidden" name="_charset_" value="utf-8">
        <input type="hidden" name="product-path" value="<%= productPath %>"/>
        <input type="radio" name="smartlist-select" value="existing" id="<%= smartlistSelectExistingId %>"
               checked="checked">
        <label for="<%= smartlistSelectExistingId %>"><%= xssAPI.filterHTML(i18n.get("Select a Smart List")) %></label>
        &nbsp;
        <input type="radio" name="smartlist-select" value="new" id="<%= smartlistSelectNewId %>">
        <label for="<%= smartlistSelectNewId %>"><%= xssAPI.filterHTML(i18n.get("Create a new Smart List")) %></label>

        <% if (smartLists != null && smartLists.size() > 0) { %>
        <div class="left existing-list">
            <label for="<%= smartlistSelectExistingSelectId %>">Smart List</label>
            <select name="smartlist-path" id="<%= smartlistSelectExistingSelectId %>">
                <% for (SmartList smartList : smartLists) { %>
                <option value="<%= smartList.getPath() %>"><%= smartList.getTitle() %></option>
                <% } %>
            </select>
        </div>
        <% } %>

        <div class="left new-list">
            <label for="<%= smartlistSelectNewTitleId %>"><%= xssAPI.filterHTML(i18n.get("Name")) %></label>
            <input type="text" name="title"
                   placeholder="<%= xssAPI.filterHTML(i18n.get("My Smart-List")) %>"
                   id="<%= smartlistSelectNewTitleId %>">
            <label for="<%= smartlistSelectNewDefaultId %>"><%= xssAPI.filterHTML(i18n.get("Default")) %></label>
            <input type="checkbox" name="default" id="<%= smartlistSelectNewDefaultId %>">
        </div>
        <div class="left">
            <input type="submit" value="<%= xssAPI.filterHTML(i18n.get("Add to Smart List")) %>">
        </div>
    </form>
</div>

