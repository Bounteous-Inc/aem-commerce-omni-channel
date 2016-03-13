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
           import="com.day.cq.wcm.api.components.DropTarget,
                   com.day.cq.wcm.foundation.Image,
                   com.day.cq.wcm.api.WCMMode" %><%
%><%@include file="/libs/foundation/global.jsp"%><%
	Image image = new Image(resource);
%><c:set var="wcmMode"><%= WCMMode.fromRequest(request) != WCMMode.DISABLED %></c:set><%
%><c:set var="ddClassName"><%= DropTarget.CSS_CLASS_PREFIX + "image" %></c:set><%
%><c:set var="imageHasContent"><%= image.hasContent() %></c:set><%
%>
<c:choose>
    <c:when test="${!imageHasContent}">
        <c:if test="${wcmMode}">
            <img class="<c:out value='${ddClassName}'/> cq-image-placeholder" src="/etc/designs/default/0.gif">
        </c:if>
    </c:when>
    <c:otherwise>
        <cq:include script="template.jsp"/>
    </c:otherwise>
</c:choose>