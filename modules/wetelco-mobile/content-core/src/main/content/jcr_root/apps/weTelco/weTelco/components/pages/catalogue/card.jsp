<%--
  ADOBE CONFIDENTIAL

  Copyright 2015 Adobe Systems Incorporated
  All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and may be covered by U.S. and Foreign Patents,
  patents in process, and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.
--%><%
%><%@include file="/libs/granite/ui/global.jsp" %><%
%><%@page session="false"
          import="com.adobe.cq.mobile.platform.MobileResource,
                  com.adobe.cq.mobile.platform.MobileResourceType,
                  com.adobe.granite.security.user.util.AuthorizableUtil,
                  com.adobe.granite.ui.components.AttrBuilder,
                  com.adobe.granite.xss.XSSAPI,
                  com.day.cq.commons.date.RelativeTimeFormat,
                  com.day.cq.commons.jcr.JcrConstants,
                  com.day.cq.wcm.api.Page,
                  com.day.jcr.vault.util.Text,
                  org.apache.commons.lang3.StringUtils,
                  org.apache.sling.api.resource.Resource,
                  org.apache.sling.api.resource.ValueMap,
                  javax.jcr.RepositoryException,
                  javax.jcr.Session,
                  javax.jcr.security.AccessControlManager,
                  javax.jcr.security.Privilege,
                  java.util.ArrayList,
                  java.util.Calendar,
                  java.util.List,
                  java.util.ResourceBundle"%><%

    /**
     * Render the resource as a DPS App card as viewed on the app catalogue page.
     */

    AccessControlManager acm = null;
    try {
        acm = resourceResolver.adaptTo(Session.class).getAccessControlManager();
    } catch (RepositoryException e) {
        log.error("Unable to get access manager", e);
    }

    Resource realResource = resource;
    Page pageResource = resource.adaptTo(Page.class);

    if (pageResource == null && resource.getName().equals(JcrConstants.JCR_CONTENT)) {
        realResource = resource.getParent();
        pageResource = realResource.adaptTo(Page.class);
    }
    if (pageResource == null) {
        return;
    }

    MobileResource appInstance = realResource.adaptTo(MobileResource.class);

    ResourceBundle rb = slingRequest.getResourceBundle(slingRequest.getLocale());
    RelativeTimeFormat rtf = new RelativeTimeFormat("r", rb);

    String xssLastModified = "";
    Calendar lastModified = pageResource.getLastModified();
    if (lastModified != null) {
        xssLastModified = rtf.format(pageResource.getLastModified().getTimeInMillis(), true);
    }

    String xssModifiedBy = pageResource.getLastModifiedBy();
    if (StringUtils.isNotBlank(xssModifiedBy)) {
        xssModifiedBy = AuthorizableUtil.getFormattedName(resourceResolver, xssModifiedBy);
    }

    String appName = xssAPI.encodeForHTML(pageResource.getName());
    String xssTitle = xssAPI.encodeForHTML(pageResource.getTitle());
    if (xssTitle == null) {
        xssTitle = appName;
    }
    String xssThumbnailUrl = getThumbnailUrl(realResource, xssAPI, request);
    String xssUrl = getResourceUrl(pageResource, xssAPI, request);

    ValueMap properties = pageResource.getProperties();

    AttrBuilder attrBuilder = new AttrBuilder(request, xssAPI);
    attrBuilder.addOther("path", pageResource.getPath());
    attrBuilder.addClass("foundation-collection-item");
    attrBuilder.addOther("foundation-collection-item-id", pageResource.getPath());
    attrBuilder.addClass("card-board cq-apps-AppInstanceCard cq-apps-dps-app");

%><article <%=attrBuilder.build()%>>
    <a href="<%= xssUrl %>">
        <div class="label">
            <h4 class="foundation-collection-item-title"><%= xssTitle %></h4>
            <div class="info u-coral-clearFix">
                <p class="info-time"><%= xssLastModified %></p>
                <p class="info-users">
                    <i><%=i18n.get("Modified By {0}", "Modified By {user}", xssModifiedBy)%></i>
                </p>
            </div>
        </div>
        <div class="image">
            <img class="wide" src="<%= xssThumbnailUrl %>" alt="">
        </div>
    </a>
    <div class="foundation-collection-quickactions" data-foundation-collection-quickactions-rel="<%=StringUtils.join(getActionRels(realResource, acm), " ")%>">
    </div>
</article><%!

    private String getResourceUrl(Page pageResource, XSSAPI xssAPI, HttpServletRequest request) {
        String pagePath = Text.escapePath(pageResource.getPath());

        MobileResource mobileResource = pageResource.adaptTo(MobileResource.class);
        if (mobileResource.isA(MobileResourceType.INSTANCE.getType())) {
            return xssAPI.getValidHref("/libs/mobileapps/admin/content/dashboard.html" + pagePath);
        }

        return xssAPI.getValidHref(request.getContextPath() + "/aem/apps.html" + pageResource.getPath());
    }

    private String getThumbnailUrl(Resource pageResource, XSSAPI xssAPI, HttpServletRequest request) {
        String ck = getCacheKiller(pageResource);
        if (ck.isEmpty()) {
            try {
                ValueMap metadata = pageResource.getChild(JcrConstants.JCR_CONTENT + "/image").adaptTo(ValueMap.class);
                String fileReference = metadata.get("fileReference", String.class);
                if (fileReference != null) {
                    Resource fileRefRes = pageResource.getResourceResolver().resolve(fileReference);
                    if (fileRefRes != null) {
                        ck = getCacheKiller(fileRefRes);
                    }
                }
            } catch(Exception ex) {
                ck = "";
            }
        } else {
            return request.getContextPath() + "/libs/mobileapps/dps/gui/resources/icons/dps_logo.png";
        }

        return xssAPI.getValidHref(request.getContextPath() + Text.escapePath(pageResource.getPath()) + ".thumb.319.319.png?ck=" + ck);
    }

    private String getCacheKiller(Resource resource) {
        String ck = "";
        try {

            ValueMap metadata = null;
            if (resource != null && resource.isResourceType("cq:Page")) {
                metadata = resource.getChild("image/_dam_thumbnail_319.png/jcr:content").adaptTo(ValueMap.class);
            } else if (resource.isResourceType("dam:Asset")) {
                metadata = resource.getChild("jcr:content").adaptTo(ValueMap.class);
            }

            if (metadata != null) {
                Calendar cal = metadata.get("jcr:lastModified", Calendar.class);
                if (cal != null) {
                    ck = "" + (cal.getTimeInMillis() / 1000);
                }
            }
        } catch(Exception ex) {
            ck = "";
        }

        return ck;
    }

    private List<String> getActionRels(Resource resource, AccessControlManager acm) {
        List<String> actionRels = new ArrayList<String>();

        if (hasPermission(acm, resource, Privilege.JCR_REMOVE_NODE)) {
            actionRels.add("cq-apps-admin-actions-delete-app-activator");
        }

        Resource content = resource.getChild(JcrConstants.JCR_CONTENT);
        ValueMap properties = content.getValueMap();

        if (properties.containsKey("phonegapConfig")) {
            actionRels.add("cq-apps-admin-actions-pgbuildremote-activator");
            actionRels.add("cq-apps-admin-actions-downloadcli-activator");
        }

        // Add an unassigned activator or else the user could get ALL actions if none are set for them.
        if (actionRels.isEmpty()) {
            actionRels.add("cq-apps-no-activators");
        }

        return actionRels;
    }

    private boolean hasPermission(AccessControlManager acm, Resource resource, String privilege) {
        if (acm != null) {
            try {
                Privilege p = acm.privilegeFromName(privilege);
                return acm.hasPrivileges(resource.getPath(), new Privilege[] { p });
            } catch (RepositoryException ignore) {
            }
        }
        return false;
    }

%>
