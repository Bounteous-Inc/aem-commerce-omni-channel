/*
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2016 Adobe Systems Incorporated
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
 */
/* globals Pjax ScreensScroller */
(function($) {
    'use strict';

    window.pjax = new Pjax({
        elements: 'a[href]:not([href="#"]):not(.no-pjax)',
        selectors: ['.wr-Page'],
        switches: {
            '.wr-Page': function(oldEl, newEl, options) {
                var scripts = newEl.outerHTML.match(/class=".*dynamicmediaimage.*"/);
                if (scripts && !$('[data-namespace="s7viewers"]').length) {
                    throw new Error('Triggering a page reload for dynamicmedia scripts to load properly');
                }
                // Following code does not seem to work
                // $.ajaxSetup({async: false});
                // oldEl.outerHTML = newEl.outerHTML;
                // scripts.map(function(script) {
                //     var scriptPath = script.match(/src=['"](.*)["']/);
                //     if (!scriptPath) {
                //         var code = script.replace(/<(script)\b[^>]*>/, '').replace(/<\/script>/, '');
                //         eval(code);
                //     }
                //     else {
                //         $.getScript(scriptPath[1]);
                //     }
                // });
                // $.ajaxSetup({async: true});
                Pjax.switches.outerHTML.call(this, oldEl, newEl, options);
            }
        },
        switchesOptions: { // ignored for now, needs Pjax.switches.sideBySide
            '.wr-Page': {
                classNames: {
                    // class added on the element that will be removed
                    remove: 'animated fadeOut',
                    // class added on the element that will be added
                    add: 'animated fadeIn',
                    // class added on the element when it go backward
                    backward: 'fadeOut',
                    // class added on the element when it go forward (used for new page too)
                    forward: 'fadeIn'
                }
            }
        }
    });

    var isMenuOpen;
    $(document).on('click', '.wr-Navigation-back', function(ev) {
        isMenuOpen = $('.wr-Menu').hasClass('wr-Menu--open');
    });

    $(document).on('pjax:complete', function(ev) {
        if (isMenuOpen) {
            $('.wr-Menu-button').trigger('click');
        }
        isMenuOpen = false;
    });

    $(document).on('ready pjax:complete', function(ev) {
        $('.u-showAfterLoad').css('visibility', 'visible');

      (function(scrollEl) {

          if (!scrollEl) {
              return;
          }

          var aScreensScroller = new ScreensScroller(scrollEl, {
              direction: ScreensScroller.DIRECTION_X,
              edgeMargin: [0, 0],
              snap: false,
              maxPointers: 2,
              panDelegate: {
                  capturePointers: function(recognizer) {
                      return false;
                  }
              },
              onTapHandler: function(e) {
                  window.pjax.loadUrl($(e.target).closest('.wr-Navigation-link').attr('href'), {history: true});
              }
          });

          window.requestAnimationFrame(function() {
              aScreensScroller.recalculateSize();
              aScreensScroller.invalidateAll();
          });

      }(document.querySelector('.wr-Navigation-list')));

        (function(scrollEl) {

            if (!scrollEl) {
                return;
            }

            var aScreensScroller = new ScreensScroller(scrollEl, {
                direction: ScreensScroller.DIRECTION_X,
                edgeMargin: [0, 0],
                snap: false,
                maxPointers: 2,
                panDelegate: {
                    capturePointers: function(recognizer) {
                        return false;
                    }
                },
                onTapHandler: function(e) {
                    window.pjax.loadUrl($(e.target).closest('.wr-ProductGrid-entry').attr('href'), {history: true});
                }
            });

            window.requestAnimationFrame(function() {
                aScreensScroller.recalculateSize();
                aScreensScroller.invalidateAll();
            });

        }(document.querySelector('.wr-ProductGrid')));

    });

}(window.jQuery));
