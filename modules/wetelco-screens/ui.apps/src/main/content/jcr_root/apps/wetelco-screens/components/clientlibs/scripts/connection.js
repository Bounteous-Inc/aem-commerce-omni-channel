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

    var HELPER_SERVLET = '/bin/nrf2016/helper.json';

    // should read this somehow from the player's display if possible
    var SCREEN_ID = '/content/screens/we-retail/locations/demo/flagship/single';

    var PRODUCT_FINDER = '/content/screens/we-retail/apps/product-catalog/en/jcr:content.productfinder.json';

    var CLS_CONNECTED = 'wr-Connection-icon--connected';

    var CLS_DISCONNECTED = 'wr-Connection-icon--disconnected';

    var customerConnected = false;

    var $phoneEl = $('.wr-Connection-icon--phone');

    function openProduct(productPath) {
        console.log('open product: ', productPath);

        var productFinderURL = PRODUCT_FINDER + '?productPath=' + encodeURI(productPath);
        $.get(productFinderURL, function(data) {
            var catalogProductPath = data.path;
            window.location.href = catalogProductPath + '.html';
        });
    }

    function processMessages(msgs) {
        if (!msgs || msgs.length === 0) {
            return;
        }
        // currently only process the last message
        var msg = msgs[msgs.length - 1];
        var data;
        try {
            data = JSON.parse(msg);
        } catch (e) {
            console.error('invalid message', data, e);
            return;
        }
        if (data.cmd === 'openProduct') {
            openProduct(data.path);
        } else {
            console.log('unkown message' + data);
        }
    }
    function checkConnection(data) {
        var isConnected = false;
        for (var i = 0; i < data.screens.length; i++) {
            var s = data.screens[i];
            if (s.id === SCREEN_ID) {
                // filter all connections
                for (var j = 0; j < s.connections.length; j++) {
                    var c = s.connections[j];
                    // hack to filter out 'sales-assistant connections'
                    if (c.clientId.substring(0, 2) === 'sa') {
                        // process messages
                        processMessages(c.messages);
                    } else {
                        isConnected = true;
                    }
                }
            }
        }
        if (customerConnected === isConnected) {
            return;
        }
        customerConnected = isConnected;

        if (customerConnected) {
            $phoneEl.removeClass(CLS_DISCONNECTED);
            $phoneEl.addClass(CLS_CONNECTED);
        } else {
            $phoneEl.removeClass(CLS_CONNECTED);
            $phoneEl.addClass(CLS_DISCONNECTED);
        }
    }

    function getConnectionStatus() {

        $.get(HELPER_SERVLET, {
        }, function(data, status) {
            if (status === 'success') {
                checkConnection(data);
            }
        }).then(function() {
            setTimeout(getConnectionStatus, 2000);
        }).fail(function() {
            setTimeout(getConnectionStatus, 2000);
        });
    }

    if ($phoneEl.length > 0) {
        console.log('connection checker enabled.');
        getConnectionStatus();
    }

    $(document).on('click', '.wr-Connection-icon--help,.wr-Button--callClerk', function() {
        $('.wr-Connection').addClass('wr-Connection--hidden');
        $('.wr-Notification--callForHelp').addClass('wr-Notification--visible');
        $('.wr-Notification-overlay').show();
    });

    $(document).on('click', '.wr-Notification-button', function(ev) {
        $(ev.target).closest('.wr-Notification').removeClass('wr-Notification--visible');
    });

    $(document).on('click', '.wr-Notification-button[data-action="call"]', function(ev) {
        $('.wr-Notification--helpComing').addClass('wr-Notification--visible');
    });

    $(document).on('click', '.wr-Notification-button[data-action="cancel"]', function(ev) {
        $('.wr-Connection').removeClass('wr-Connection--hidden');
        $('.wr-Notification-overlay').hide();
    });

    $(document).on('click', '.wr-Notification-overlay,.wr-Menu', function(ev) {
        if ($('.wr-Notification--visible').length) {
            $('.wr-Notification--visible').removeClass('wr-Notification--visible');
            $('.wr-Connection').removeClass('wr-Connection--hidden');
            $('.wr-Notification-overlay').hide();
        }
    });

}(window.jQuery));
