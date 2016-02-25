<%@include file="/libs/foundation/global.jsp" %><%
%><%@ page session="false" %><%
%>
<div class="list">
		<a class="item">
		<h5>Data</h5>
		<div style="background: black"><div style="height: 30px; width: {{accounts.plandatausagepercent}}%; transition: width 0.1s; background: #a0148c;"></div></div>
		<p>{{accounts.plandatausagevalue}}Gb of {{accounts.plandatalimit}}Gb used</p>
		</a>				
</div>