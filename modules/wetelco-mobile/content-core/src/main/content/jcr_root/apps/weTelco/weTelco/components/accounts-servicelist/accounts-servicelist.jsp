<%@include file="/libs/foundation/global.jsp" %><%
%><%@ page session="false" %><%
%>
<ion-list ng-controller="AccountsCtrl">

   <ion-item ng-repeat="account in accounts.services" class="item-icon-left">
     <i class="icon ion-iphone"></i>
      <h2>{{account.plan}}</h2>
      <p>{{account.phone}} - {{account.plantype}}</p>
      <ion-option-button class="button-positive"
                       ng-click="share(item)">
                         Rename
      </ion-option-button>
      <ion-reorder-button class="ion-navicon"
                        on-reorder="reorderItem(item, $fromIndex, $toIndex)">
      </ion-reorder-button>
  </ion-item>
</ion-list>