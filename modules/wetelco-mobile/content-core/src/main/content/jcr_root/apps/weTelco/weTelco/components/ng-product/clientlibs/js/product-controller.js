/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 *  Copyright 2013 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
;(function (angular, undefined) {

    "use strict";

    angular.module('cqProduct', []);

    function ProductCtrl($scope) {

        $scope.addToCartClickHandler = function(event) {
            var addToCartImage = angular.element(event.currentTarget);
            // Skip code if we already signalled that this is in the shopping cart.  We will have to think
            // about how to add items to the shopping cart multiple times.
            if (addToCartImage.hasClass('clear')) {
                return true;
            }
            var addToCartContainer = addToCartImage.parent();
            var addToCartImages = addToCartContainer.find('img');
            addToCartImages.each(function(index) {
                if ($(this).hasClass('clear')) {
                    $(this).removeClass('clear');
                } else {
                    $(this).addClass('clear');
                }
            });

            // TODO: Add 1 of this item to the shopping cart.

            event.preventDefault();
            event.stopPropagation();
            return false;
        }
    };

    angular.module('cqProduct')
        .controller('ProductCtrl', ["$scope", ProductCtrl])

})(angular);