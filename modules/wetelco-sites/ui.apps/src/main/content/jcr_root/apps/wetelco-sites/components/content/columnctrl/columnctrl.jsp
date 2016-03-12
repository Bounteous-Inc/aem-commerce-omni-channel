 <%--

  Title Box component.

--%>

<%@include file="/libs/foundation/global.jsp"%>
<%@page import="com.day.cq.wcm.api.WCMMode, com.day.cq.wcm.foundation.Placeholder" %>
<%@page session="false"%>
<%
    WCMMode mode = WCMMode.fromRequest(slingRequest);
	String[] columns = (String[]) properties.get("columns", String[].class);
	String bgColor = properties.get("bgcolor", "inherit");
%>
<div style="width: 100%; background-color:#<%=bgColor%>">
<div class="row normal-row edit-columnctrl">
<%
if (WCMMode.EDIT.equals(mode)) {
%>
    <div class="col-xs-12">
        <center></center>
    </div>
<%
}
        if(columns != null){
            int tmpTotal = 0;
            for(int i = 0; i < columns.length; i++ ) {
                String column = columns[i].trim();
                if(column.equals("")){
                    continue;
                }

    %>
        <div class="col-sm-<%=column%> col-xs-12">
            <cq:include path="<%="par_"+i%>" resourceType="foundation/components/parsys" />
		</div>
    <%

			}

        }else{
    %>
    	<div>
            <p style="text-align:center">Edit the component to set the columns.</p>
    	</div>    
    <%
        }
    %>

</div>
</div>