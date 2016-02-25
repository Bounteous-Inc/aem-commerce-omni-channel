<%--
  ADOBE CONFIDENTIAL
  __________________

   Copyright 2016 Adobe Systems Incorporated
   All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.
--%><%@ page import="com.adobe.cq.screens.demo.nrf2016.ScreenConnectionManager" %>
<%@include file="/libs/foundation/global.jsp"%><%

    // this should come from the display... and should probably be in JS
    String SCREEN_ID = "/content/screens/we-retail/locations/demo/flagship/single";

    ScreenConnectionManager mgr = sling.getService(ScreenConnectionManager.class);
    ScreenConnectionManager.Screen s = mgr.getScreen(SCREEN_ID);
    boolean isConnected = s != null && !s.getConnections().isEmpty();
    String cls = isConnected ? "connected" : "disconnected";
%>
<div class="wr-Connection">
    <button class="wr-Connection-icon wr-Connection-icon--phone wr-Connection-icon--<%= cls %>"></button>
    <button class="wr-Connection-icon wr-Connection-icon--help"></button>
</div>
<div class="wr-Notification-overlay"></div>
<div class="wr-Notification wr-Notification--callForHelp">
    <p class="wr-Notification-message">Questions? Call for help.</p>
    <bouton class="wr-Notification-button" data-action="call">Yes</bouton>
    <bouton class="wr-Notification-button" data-action="cancel">No</bouton>
</div>
<div class="wr-Notification wr-Notification--helpComing">
    <p class="wr-Notification-message">Someone is on the way!</p>
    <bouton class="wr-Notification-button" data-action="cancel">Ok</bouton>
</div>
