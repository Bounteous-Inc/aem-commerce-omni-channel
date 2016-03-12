<%@page session="false" %><%
%><%@include file="/libs/foundation/global.jsp" %><%
%>
<cq:includeClientLib css="weTelco.ionic-1.1.0"/>
<cq:includeClientLib css="apps.weTelco.weTelco.all"/>

<!-- Enable all requests, inline styles, and eval() -->
<!-- TODO: set a more restrictive CSP for production -->
<meta http-equiv="Content-Security-Policy" content="default-src *; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline' 'unsafe-eval'">
