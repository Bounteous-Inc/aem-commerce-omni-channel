/*
 * ADOBE CONFIDENTIAL
 *
 * Copyright 2016 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */
function productAddWishlist_init($) {
    'use strict';

    var SMARTLIST_MANAGER_SUFFIX = '.commerce.smartlist.management.html';
    var PRODUCT_SUFFIX = '/jcr:content/product';
    var PROJECT_SMARTLIST = 'we-retails-smartlist';
    var IS_IN_SMARTLIST_ATTR = 'isinsmartlist';

    function isInSmartList(element) {
        return element.data(IS_IN_SMARTLIST_ATTR);
    }

    function toggle(element) {
        var newIsInSmartList = !isInSmartList(element);
        element.data(IS_IN_SMARTLIST_ATTR, newIsInSmartList);
        element.html(newIsInSmartList ? 'Remove from WishList' : 'Add to WishList');
    }

    function addToList(path, element) {
        $.post(path + SMARTLIST_MANAGER_SUFFIX, {
            'product-path': path + PRODUCT_SUFFIX,
            ':operation': 'addToSmartList',
            'redirect': path + '.html',
            'smartlist-path': PROJECT_SMARTLIST
        }, function(data, status) {
            if (status === 'success') {
                toggle(element);
            }
        });
    }

    function removeFromList(path, element) {
        $.post(path + SMARTLIST_MANAGER_SUFFIX, {
            'product-path': path + PRODUCT_SUFFIX,
            ':operation': 'deleteSmartListEntry',
            'redirect': path + '.html',
            'smartlist-path': PROJECT_SMARTLIST
        }, function(data, status) {
            if (status === 'success') {
                toggle(element);
            }
        });
    }

    $(document).on('click', '.js-add-product-to-wishlist', function(ev) {
        var path = ev.currentTarget.dataset.path;
        var element = $(ev.target);

        if (!isInSmartList(element)) {
            addToList(path, element);
        } else {
            removeFromList(path, element);
        }

    });
}

productAddWishlist_init(window.$);
