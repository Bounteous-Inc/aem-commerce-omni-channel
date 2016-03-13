<%@include file="/libs/foundation/global.jsp" %><%
%><%@ page session="false" %><%
%>
<div class="item item-divider">Account Details</div>
<ion-list ng-controller="AccountsCtrl">

   <ion-item  class="item-icon-left item-icon-right">
     <i class="icon ion-iphone"></i>
      <h2>Account: {{accounts.accountno}}</h2>
      <p>Balance: $ {{accounts.balance}}</p>
     <i class="icon ion-gear-a"></i>
  </ion-item>
</ion-list>