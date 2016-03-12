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

  ==============================================================================

  Shows the custom proeprties of a shopping cart entry.
  The default implementaion is empty.
  Derived components can override this script to show the desired custom components.

  request attributes:
  - {com.adobe.cq.commerce.api.CommerceSession.CartEntry} cart_entry The shopping cart entry to show.
  - {java.util.Map<Integer, java.util.List<com.adobe.cq.commerce.api.promotion.PromotionInfo>>} entry_promotions The
  map of sopping cart enry promotions groupped by entry number.

  ==============================================================================

--%>
<%@include file="/libs/foundation/global.jsp"%>
<%@page session="false" import="
		com.adobe.cq.commerce.api.CommerceSession,
		com.day.cq.i18n.I18n,
		com.day.cq.wcm.foundation.forms.FormsConstants,org.apache.commons.lang.StringUtils,
		java.util.ResourceBundle,
		java.util.Locale"
%><%
    final Locale pageLocale = currentPage.getLanguage(false);
    final ResourceBundle bundle = slingRequest.getResourceBundle(pageLocale);
    final I18n i18n = new I18n(bundle);

    boolean readOnly = properties.get(FormsConstants.ELEMENT_PROPERTY_READ_ONLY, false);
    CommerceSession.CartEntry entry = (CommerceSession.CartEntry) request.getAttribute("cart_entry");
%>
<cq:includeClientLib categories="geometrixx-outdoors.shoppingcart" />
<br>
<%
    Boolean wrapping = entry.getProperty("wrapping-selected", Boolean.class);
    if (wrapping == null) {
        wrapping = false;
    }
    String label = entry.getProperty("wrapping-label", String.class);
    if (label == null) {
        label = "";
    }
    if (wrapping) { %>
        <%if (StringUtils.isNotBlank(label)) {%>
        <span class="promotion"><%= i18n.get("Gift wrapping message: ") %><%= xssAPI.filterHTML(label) %></span>
        <%} else {%>
        <span class="promotion"><%= i18n.get("Gift wrapping selected")%></span>
        <%}
    }

    if (!readOnly) { %>
<div>
    <% if (wrapping) { %>
    <a class="edit-wrapping-link" href="#"><%=i18n.get("Modify gift wrapping...")%><%
    } else { %>
    <a class="edit-wrapping-link" href="#"><%=i18n.get("Add gift wrapping...")%><%
    }
    String selectId = "cartEntryProperties-wrapping-select-" + entry.getEntryIndex();
    String labelId = "cartEntryProperties-wrapping-label-" + entry.getEntryIndex();
    %>
        <div style="display: none">
            <div class="cartEntryProperties-wrapping-form">
                <form method="POST" action="<%= resource.getPath() %>.modify.html">
                    <input type="hidden" name="_charset_" value="utf-8">
                    <div class="wrapping-form-left">
                        <label for="<%=selectId%>"><%= i18n.get("Enabled") %></label>
                    </div>
                    <div class="wrapping-form-right">
                        <input id="<%=selectId%>" type="checkbox" name="wrapping" value="true" <%=wrapping?"checked":""%>/>
                    </div>
                    <div class="wrapping-form-left">
                        <label for="<%=labelId%>"><%= i18n.get("Message") %></label>
                    </div>
                    <div class="wrapping-form-right">
                        <input id="<%=labelId%>" type="text" name="label" size="20" value="<%= xssAPI.filterHTML(label) %>"/>
                    </div>
                    <div class="wrapping-form-submit">
                        <input type="hidden" name="quantity" value="<%= entry.getQuantity() %>"/>
                        <input type="hidden" name="entryNumber" value="<%= entry.getEntryIndex() %>"/>
                        <input type="hidden" name="redirect" value="<%= xssAPI.encodeForHTMLAttr(resourceResolver.map(request, currentPage.getPath()) + ".html") %>"/>
                        <input class="wrapping-submit-button" type="submit" value="<%= i18n.get("Update") %>"/>
                    </div>
                </form>
            </div>
        </div>
    </a>
</div>
<% } %>
