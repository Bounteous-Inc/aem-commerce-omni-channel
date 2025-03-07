<%@page session="false"%><%--
  Copyright 1997-2008 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================

  Logo component

  Includes an image from the design and draws a link to the home page.

--%><%@include file="/libs/foundation/global.jsp"%><%
%><%@ page import="com.day.text.Text,
                   com.day.cq.wcm.foundation.Image,
                   com.day.cq.commons.Doctype" %><%

    int absParent = 2;
    Page homePage = currentPage.getAbsoluteParent(absParent);
    String home = homePage != null ? homePage.getPath() : Text.getAbsoluteParent(currentPage.getPath(), absParent);
    String margin = currentStyle.get("img_margin", "auto");
    Resource res = currentStyle.getDefiningResource("imageReference");
    if (res == null) {
        res = currentStyle.getDefiningResource("image");
    }
    %><style>
	.navbar-brand > img {
        margin: <%=margin%>;
    }
</style>
<a class="navbar-brand" href="<%= home %>/personal.html" style="margin-top: -18px;"><%
    if (res == null) {
        %> <%
    } else {
        Image img = new Image(res);
        img.setItemName(Image.NN_FILE, "image");
        img.setItemName(Image.PN_REFERENCE, "imageReference");
        img.setSelector("img");
        img.setDoctype(Doctype.fromRequest(request));
        // img.setAlt(homePage.getTitle());
        img.draw(out);
    }
    %></a>
