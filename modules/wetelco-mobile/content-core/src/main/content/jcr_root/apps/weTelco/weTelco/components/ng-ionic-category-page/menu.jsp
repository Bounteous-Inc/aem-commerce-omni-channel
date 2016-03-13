<%@page session="false" %>
<div class="list">
  <a class="item menu-close item-icon-left" ng-click="go('/content/phonegap/weTelco/en/home')">
 	<i class="icon ion-ios-pie-outline"></i>
        My Usage
    </a>
  <a class="item menu-close item-icon-left energised" ng-click="go('/content/phonegap/weTelco/en/home/my-bills')">
    <i class="icon ion-ios-paper-outline"></i>
    My Bills
  </a>
  <a class="item menu-close item-icon-left" ng-click="go('/content/phonegap/weTelco/en/home/offers')">
    <i class="icon ion-ios-star-outline"></i>
    Offers
  </a>
  <a class="item menu-close item-icon-left" ng-click="go('/content/phonegap/weTelco/en/home/find-us')">
    <i class="icon ion-ios-location-outline"></i>
    Find Us
  </a>
  <a class="item menu-close item-icon-left" ng-click="go('/content/phonegap/weTelco/en/home/help---support')">
    <i class="icon ion-ios-help-outline"></i>
    Help & Support
  </a>
  <a class="item menu-close item-icon-left"  ng-click="go('/content/phonegap/weTelco/en/home/app-settings')">
    <i class="icon ion-ios-gear-outline"></i>
    Settings
  </a>    
    <a class="item menu-close item-button-right" ng-click="updateApp()">
        Update
        <button class="button button-telco">
            <i class="icon ion-ios-cloud-download"></i>
        </button>
    </a>
</div>
