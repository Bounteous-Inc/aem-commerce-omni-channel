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
--%><%
%><%@ page session="false"
           import="com.adobe.cq.mobile.angular.data.util.FrameworkContentExporterUtils" %><%
%><%@include file="/libs/foundation/global.jsp"%><%
%><c:set var="componentDataPath"><%= FrameworkContentExporterUtils.getJsFriendlyResourceName(resource.getPath()) %></c:set><%
%>    <div ng-repeat="image in <c:out value='${componentDataPath}'/>">
    <div ng-switch on="image.hasLink" >
        <a ng-switch-when="true" ng-click="go('{{image.linkPath}}')" x-cq-linkchecker="skip">
            <img ng-src="<%= request.getContextPath() %>{{image.imgSrc}}" alt="{{image.alt}}" title="{{image.title}}" />
        </a>
            <span ng-switch-default>
                <img ng-src="<%= request.getContextPath() %>{{image.imgSrc}}" alt="{{image.alt}}" title="{{image.title}}" />
            </span>
    </div>
</div>
