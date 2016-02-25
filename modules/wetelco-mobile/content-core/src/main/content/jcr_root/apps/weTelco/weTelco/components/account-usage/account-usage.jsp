<%@include file="/libs/foundation/global.jsp" %><%
%><%@ page session="false" %><%
%>
<div class="item item-divider">Account List</div>
<div class="list">
	<div ng-repeat="account in accounts">
		<a class="item">{{account.plan}} - {{account.phone}} </a>
	</div>
</div>