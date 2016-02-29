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
(function($) {
    'use strict';

    var MENU_SELECTOR = '.wr-Menu';
    var OPEN_MENU_SELECTOR = MENU_SELECTOR + '--open';
    var MENU_BUTTON_SELECTOR = MENU_SELECTOR + '-button';

    var hidePanel = function() {
        $(MENU_SELECTOR).removeClass(OPEN_MENU_SELECTOR.substring(1));
    };

    var showPanel = function() {
        $(MENU_SELECTOR).addClass(OPEN_MENU_SELECTOR.substring(1));
    };

    var togglePanel = function() {
        var $menu = $(MENU_SELECTOR);
        $menu.is(OPEN_MENU_SELECTOR) ? hidePanel() : showPanel();
    };

    $(document).on('click', MENU_BUTTON_SELECTOR, function(ev) {
        ev.preventDefault();
        var $btn = $(MENU_BUTTON_SELECTOR);
        if ($btn.is('[href]')) {
            window.pjax.loadUrl($btn.attr('href'), {history: true});
            return;
        }
        togglePanel();
    });

}(window.jQuery));
