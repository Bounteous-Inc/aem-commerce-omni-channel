<%@page session="false"%><%--
  Copyright 1997-2009 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================

  Shows information about the currently logged in user.

--%><%@include file="/libs/foundation/global.jsp"%><%
%><%@ page import="com.adobe.cq.commerce.api.CommerceConstants,
                   com.day.cq.i18n.I18n,
				   com.day.cq.wcm.api.Page,
                   com.day.cq.personalization.UserPropertiesUtil,
                   com.day.cq.wcm.api.WCMMode,
                   java.util.Locale,
				   java.util.Iterator" %><%
%><%@taglib prefix="personalization" uri="http://www.day.com/taglibs/cq/personalization/1.0" %><%

    Locale pageLang = currentPage.getLanguage(false);
    final I18n i18n = new I18n(slingRequest.getResourceBundle(pageLang));
    final boolean isAnonymous = UserPropertiesUtil.isAnonymous(slingRequest);
    final boolean isDisabled = WCMMode.DISABLED.equals(WCMMode.fromRequest(request));
    final String logoutPath = request.getContextPath() + "/system/sling/logout.html";
	Page rootPage = currentPage.getAbsoluteParent(1);
	Page level1Page = currentPage.getAbsoluteParent(2);
	String loginPath = level1Page.getPath() + "/personal/login.html";

%>
<script type="text/javascript">function logout() {
    if (_g && _g.shared && _g.shared.ClientSidePersistence) {
        _g.shared.ClientSidePersistence.clearAllMaps();
    }

<% if( !isDisabled ) { %>
    if (CQ_Analytics && CQ_Analytics.CCM) {
        CQ_Analytics.ProfileDataMgr.loadProfile("anonymous");
        CQ.shared.Util.reload();
    }
<% } else { %>
    if (CQ_Analytics && CQ_Analytics.CCM) {
        CQ_Analytics.ProfileDataMgr.clear();
        CQ_Analytics.CCM.reset();
    }
    CQ.shared.HTTP.clearCookie("<%= CommerceConstants.COMMERCE_COOKIE_NAME %>", "/");
    CQ.shared.Util.load("<%= xssAPI.encodeForJSString(logoutPath) %>");
<% } %>
}</script>
<style>
    .userinfo-container {
        display:none;
    }
</style>
<script type="text/javascript">
    var showUserInfo = function(isAnonymous) {
			$(".userinfo-container").css("display", "block");
            if (typeof afterLogin != 'undefined') {
				afterLogin(isAnonymous);
            } 
    };

    $(function() {
		setTimeout('showUserInfo(<%=isAnonymous%>)', 500);
    });
</script>
		<ul class="nav navbar-nav navbar-right userinfo-container">
              <li class="cq-cc-profile-anonymous dropdown hidden-xs">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown"><%=i18n.get("Language")%><span class="caret"></span></a>
                <ul class="dropdown-menu" role="menu">
                  <%
    				Iterator<Page> ps = rootPage.listChildren();
					while(ps.hasNext()) {
						Page p = ps.next();
                        if (p.isHideInNav()) {continue;}
    			  %>
                  <li><a href="<%=p.getPath()%>.html"><%=p.getTitle()%></a></li>
    			  <%
                    }
                  %>
                </ul>
              </li>
        <%
            if (isDisabled) {

                //in publish mode, only display the name if !anonymous
                if (!isAnonymous) {
        %>
              <li class="dropdown hidden-xs">
				 <a href="#"><span class="glyphicon glyphicon-envelope"></span>
                 <span class="hidden-sm hidden-xs">&nbsp;<personalization:contextProfileProperty propertyName="formattedName" prefix="(" suffix=")"/></span></a>
              </li>
              <li><a href="javascript:logout();"><span class="glyphicon glyphicon-off"></span><span class="hidden-sm hidden-xs">&nbsp;<%=i18n.get("Logout")%></span></a></li>

        <%
        		} else {

        %>
				<li><a href="<%=loginPath%>"><span class="glyphicon glyphicon-user"></span><span class="hidden-sm hidden-xs">&nbsp;<%=i18n.get("Login")%></span></a></li>
        <%

            	}

        	} else {

            //on author handle link from the ContextCloud
        %>
              <li class="cq-cc-profile-not-anonymous dropdown hidden-xs">
				 <a href="#"><span class="glyphicon glyphicon-envelope"></span>
                 <span class="hidden-sm hidden-xs">&nbsp;<personalization:contextProfileProperty propertyName="formattedName" prefix="(" suffix=")"/></span></a>
              </li>

              <li class="cq-cc-profile-not-anonymous"><a href="javascript:logout();"><span class="glyphicon glyphicon-off"></span><span class="hidden-sm hidden-xs">&nbsp;<%=i18n.get("Logout")%></span></a></li>

			 <li class="cq-cc-profile-anonymous"><a href="<%=loginPath%>"><span class="glyphicon glyphicon-user"></span><span class="hidden-sm hidden-xs">&nbsp;<%=i18n.get("Login")%></span></a></li>
        <%

            }
        %>
        </ul>