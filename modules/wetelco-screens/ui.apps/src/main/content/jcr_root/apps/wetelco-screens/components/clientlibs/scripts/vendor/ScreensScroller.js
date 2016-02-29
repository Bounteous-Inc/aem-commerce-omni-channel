/*
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2014 Adobe Systems Incorporated
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
/* eslint no-use-before-define:0 */

// 'underscore', './ScrollableList', './Physics'

(function($, _) {
    'use strict';

    var ScrollableListModule = (function() {

        var STYLE_SHEET_TITLE = 'ctx-scrollablelist-styles';
        var CSS_CLASS_WORLD = 'ctx-ScrollableList-world';
        var CSS_CLASS_SCENE = 'ctx-ScrollableList-scene';
        var CSS_CLASS_ITEM_X = 'ctx-ScrollableList-item-x';
        var CSS_CLASS_ITEM_Y = 'ctx-ScrollableList-item-y';

        /**
         * Array of functions to translate an element depending on this list's direction.
         * @private
         */
        var _translateEl = [
            function(el, pos) { el.style.transform = 'translate3d(' + pos + 'px, 0px, 0px)';},
            function(el, pos) { el.style.transform = 'translate3d(0px, ' + pos + 'px, 0px)';}
        ];
        var _translateElWebKit = [
            function(el, pos) { el.style.webkitTransform = 'translate3d(' + pos + 'px, 0px, 0px)';},
            function(el, pos) { el.style.webkitTransform = 'translate3d(0px, ' + pos + 'px, 0px)';}
        ];

        /**
         * The scrollable list manages a list of items horizontally or vertically. It supports the notion of a current
         * item which an offset into the visible area (the world). This view also supports viewports in order to be able
         * to sync with other screens.
         * <pre>
         *   ___position___ |---- world ----------------------|
         * /               \|           |- viewp. -|          |
         * +................+-----------+==========+----------+---------+
         * | +--------+  +--|-----+  +--|-----+  +-|------+  +|-------+ |
         * | |        |  |  |     |  |  |     |  | |      |  ||       | |
         * | |        |  |  |     |  |  |     |  | |      |  ||       | |
         * | | first  |  |cu|rent |  |  |     |  | |      |  || last  | |
         * | |        |  |  |     |  |  |     |  | |      |  ||       | |
         * | |        |  |  |     |  |  |     |  | |      |  ||       | |
         * | +--------+  +--|-----+  +--|-----+  +-|------+  +|-------+ |
         * +................+-----------+==========+----------+---------+
         *               \  \
         *                \ offset
         * </pre>
         *
         * The position denotes the start of the world relative to the list.
         * The offset denotes the start of the world relative to the current item.
         * The viewport is relative to the world
         *
         * @class ScrollableList
         * @constructor
         * @param {HTMLElement|String} el The HTML element to scroll
         * @param {Object} [options] An object of configurable options.
         * @param {Number} [options.direction=ScrollableList.DIRECTION_X] The direction of the list.
         * @param {Number} [options.gutter=0] Additional gutter added between items
         * @param {Number} [options.margin=100] Off-screen rendering margin
         * @param {Boolean} [options.debug=false] Shows debug information
         */
        function ScrollableList(el, options) {
            this.el = el = el.jquery ? el[0] : el;
            this.options = _.extend({}, ScrollableList.DEFAULT_OPTIONS, _.pick(options || {}, Object.keys(ScrollableList.DEFAULT_OPTIONS)));

            this._direction = this.options.direction ? 1 : 0; // normalize
            this._gutter = this.options.gutter;
            this._position = 0;
            this._offset = 0;
            this._first = 0;
            this._current = 0;
            this._last = 0;
            this._lastEdge = 0;
            this._edgeHit = 0;
            this._hash = '';
            this._noJitter = true;

            _defineCSSRules();
            this._cssItemClassName = this._direction ? CSS_CLASS_ITEM_Y : CSS_CLASS_ITEM_X;

            // create world element
            var world = this._world = document.createElement('div');
            world.className = CSS_CLASS_WORLD;

            // create scene element
            var scene = this._scene = document.createElement('div');
            scene.className = CSS_CLASS_SCENE;

            world.appendChild(scene);

            // append the contents of 'el to the scene, but wrap each one with a
            // scrollable-item div. this way we don't interfere with potential
            // transformations of the inner elements
            while (el.children.length) {
                var div = document.createElement('div');
                div.className = this._cssItemClassName;
                div.appendChild(el.children[0]);
                scene.appendChild(div);
            }

            // append world back to content
            el.appendChild(world);

            // check which transform to use
            this._scene.style.transform = 'translate3d(0,0,0)';
            if (!window.getComputedStyle(this._scene).getPropertyValue('transform')) {
                _translateEl = _translateElWebKit;
            }
            this._scene.style.transform = '';

            this.recalculateSize();
            _updateCache.call(this, true);
            _initDebug.call(this);
            _layout.call(this);
        }

        /**
         * X direction (horizontal)
         *
         * @property DIRECTION_X
         * @type Number
         * @static
         * @default 0
         */
        ScrollableList.DIRECTION_X = 0;

        /**
         * Y direction (vertical)
         *
         * @property DIRECTION_Y
         * @type Number
         * @static
         * @default 1
         */
        ScrollableList.DIRECTION_Y = 1;

        /**
         * Default Options
         */
        ScrollableList.DEFAULT_OPTIONS = {
            direction: ScrollableList.DIRECTION_X,
            gutter: 0,
            margin: 100,
            debug: false,

            /**
             * the lower and upper margins of the edges. suitable
             * to place the edge springs outside the world, eg. to add
             * "bumpers" around the list
             */
            edgeMargin: [0, 0]
        };

        /**
         * Array of functions to retrieve the size of an element depending on this list's direction.
         * @static
         */
        ScrollableList.FN_GET_SIZE_FOR_EL = [
            function(el) { return el.offsetWidth;},
            function(el) { return el.offsetHeight;}
        ];

        ScrollableList.prototype = {

            /**
             * Destroys this scroll view and releases all resources
             */
            destroy: function() {
                if (this._world) {
                    this.el.removeChild(this._world);
                    this._world = null;
                }
            },

            /**
             * Returns the scroll position of this list
             * @returns {number} the scroll position in pixels
             */
            getPosition: function() {
                return this._position;
            },

            /**
             * Sets the scroll position of this list.
             * @param {number} pos the new position in pixels.
             * @param {boolean} [invalidate] true to invalidate this list and trigger a re-layout
             */
            setPosition: function(pos, invalidate) {
                this._position = pos;
                if (invalidate) {
                    _layout.call(this);
                }
            },

            /**
             * Checks the given position against the edges and returns an position that will not
             * cause the list to scroll out of the world.
             *
             * @param {number} pos position to check
             * @returns {number} a position that is save to set
             */
            limitPosition: function(pos) {
                pos = Math.min(this._lastEdge - this._edgeRight, pos);
                pos = Math.max(this._edgeLeft, pos);
                return pos;
            },

            /**
             * Returns the length of this list.
             * @returns {Number} length of this list.
             */
            length: function() {
                return _getItems.call(this).length;
            },

            /**
             * Returns the element at the specified index or null.
             * @param {Number} idx index
             * @returns {HTMLElement} the element or null
             */
            at: function(idx) {
                var item = _getItemAt.call(this, idx);
                return item === null ? null : item.children[0];
            },

            /**
             * Returns the index of the element or -1 if not found
             * @param {HTMLElement} el the element
             * @returns {number} index of the element
             */
            indexOf: function(el) {
                el = _getItem.call(this, el);
                if (el) {
                    var items = _getItems.call(this);
                    for (var i = 0, len = items.length; i < len; i++) {
                        if (items[i] === el) {
                            return i;
                        }
                    }
                }
                return -1;
            },

            /**
             * Returns the position of the given element. The first element always has position 0.
             * @param {HTMLElement|Number} el Element or index
             * @return {number} the position of the element.
             */
            positionOf: function(el) {
                el = _getItem.call(this, el);
                return el && el.scrollListCache && el.scrollListCache.pos;
            },

            /**
             * Returns the size of the given element.
             * @param {HTMLElement|Number} el Element or index
             * @return {number} the size of the element.
             */
            sizeOf: function(el) {
                el = _getItem.call(this, el);
                return el && el.scrollListCache && el.scrollListCache.size;
            },

            /**
             * Get the current item. The current item is the first visible one.
             *
             * @returns {Integer} the index of the current item.
             */
            getCurrentIdx: function() {
                return this._current;
            },

            /**
             * Returns information about an item
             * @param {HTMLElement|Number} el Element
             * @returns {Object} Information object about an item
             *              info.idx Index
             *              info.el  Element
             *              info.position  Position
             *              info.size Size
             *              info.nextPos Next Pos
             *              info.isLast Is Last
             */
            infoOf: function(el) {
                var idx = el;
                if (typeof el === 'number' || typeof el === 'string') {
                    el = _getItemAt.call(this, +el);
                } else {
                    el = _getItem.call(this, el);
                    idx = this.indexOf(el);
                }
                if (!el || !el.scrollListCache) {
                    return null;
                }

                var cache = el.scrollListCache;
                return {
                    idx: idx,
                    el: el.children[0],
                    position: cache.pos,
                    size: cache.size,
                    nextPos: cache.pos + cache.size + this.options.gutter,
                    isLast: idx === _getItems.call(this).length - 1
                };
            },

            /**
             * Invalidates a item
             * @param {...HTMLElement} [el] Element(s) to invalidate
             */
            invalidate: function(el) {
                for (var i = 0; i < arguments.length; i++) {
                    var item = _getItem.call(this, arguments[i]);
                    if (item && item.scrollListCache) {
                        item.scrollListCache.size = 0;
                    }
                }
                this._hash = '';
                _updateCache.call(this);
                _layout.call(this);
            },

            /**
             * recalculates the world sizes after the window changed.
             */
            recalculateSize: function() {
                // get left and right off-screen margins. items that fall outside of that are not rendered
                var worldSize = this._worldSize = ScrollableList.FN_GET_SIZE_FOR_EL[this._direction](this._world);
                this._marginLeft = -this.options.margin;
                this._marginRight = this.options.margin + worldSize;

                // left and right edges are where the world ends
                this._edgeLeft = this.options.edgeMargin[0];
                this._edgeRight = worldSize + this.options.edgeMargin[1];
            },

            /**
             * Invalidates the internal cache and recalculates all sizes
             */
            invalidateAll: function() {
                this._hash = '';
                _updateCache.call(this, true);
                _layout.call(this);
            },

            /**
             * Binds an event listener to the element of this list
             *
             * @param {String} eventNames The event to bind to
             * @param {Function} fn The function to call
             */
            on: function(eventNames, fn) {
                var self = this;
                eventNames.split(/\s+/).forEach(function(name) {
                    self.el.addEventListener(name, fn, false);
                });
            },

            /**
             * Removes an event listener to the element of this list
             * @param {String} eventNames The event to unbind
             * @param {Function} fn The function to unbind
             */
            off: function(eventNames, fn) {
                var self = this;
                eventNames.split(/\s+/).forEach(function(name) {
                    self.el.removeEventListener(name, fn, false);
                });
            },

            /**
             * Returns the elements of this list
             * @returns {HTMLElement[]} elements of this list
             */
            children: function() {
                var ret = [];
                var items = _getItems.call(this);
                for (var i = 0, len = items.length; i < len; i++) {
                    ret.push(items[i].children[0]);
                }
                return ret;
            },

            /**
             * The splice() method changes the content of this list, adding new elements while removing old elements.
             *
             * @param {number} [start=0]
             *                       Index at which to start changing the array. If greater than the length of the array,
             *                       actual starting index will be set to the length of the array.
             *                       If negative, will begin that many elements from the end.
             * @param {number} [deleteCount='all']
             *                       An integer indicating the number of old array elements to remove. If deleteCount is 0,
             *                       no elements are removed. In this case, you should specify at least one new element.
             *                       If deleteCount is greater than the number of elements left in the array starting at
             *                       index, then all of the elements through the end of the array will be deleted.
             *                       If deleteCount is undefined, all elements after start are removed.
             * @param {HTMLElement|HTMLElement[]} elements
             *                       The elements to add to the list. If you don't specify any elements,
             *                       splice simply removes elements from the array.
             *
             * @example
             *   splice()                          - removes all elements
             *   splice(item)                      - inserts one item at the beginning
             *   splice(Number.MAX_VALUE, 0, item) - inserts one item at the end
             *   splice(-1, 1, item)               - replaces the last element
             *
             * @returns {HTMLElement[]}
             *                       An array containing the removed elements.
             *                       If only one element is removed, an array of one element is returned.
             *                       If no elements are removed, an empty array is returned.
             *
             */
            splice: function(start, deleteCount, elements) {
                var ret = [];
                var items = _getItems.call(this);
                var firstItemArg = 2;
                var idx = start;
                var numDelete = deleteCount;
                var item;
                if (arguments.length === 0) {
                    idx = 0;
                    numDelete = items.length;
                } else if (typeof start !== 'number') {
                    idx = 0;
                    numDelete = 0;
                    firstItemArg = 0;
                } else if (typeof deleteCount !== 'number') {
                    numDelete = items.length;
                    firstItemArg = 1;
                }
                if (idx < 0) {
                    idx = Math.max(0, idx + items.length);
                }

                // remember current info if list is manipulated before the selected index
                var currentInfo = null;
                if (idx <= this._current) {
                    currentInfo = this.infoOf(this._current);
                    // console.log("current info:", currentInfo)
                }

                while (idx < items.length && numDelete > 0) {
                    numDelete--;
                    item = items[idx];
                    this._scene.removeChild(item);
                    ret.push(item.children[0]);
                }
                var next = idx < items.length ? items[idx] : null;
                for (var i = firstItemArg; i < arguments.length; i++) {
                    var arg = arguments[i];
                    if (_.isArray(arg) || arg.jquery) {
                        for (var j = 0; j < arg.length; j++) {
                            _addElement.call(this, arg[j], next);
                        }
                    } else {
                        _addElement.call(this, arg, next);
                    }
                }

                if (currentInfo) {
                    _updateCache.call(this, true);
                    item = _getItem.call(this, currentInfo.el);
                    if (item && item.scrollListCache) {
                        this.setPosition(item.scrollListCache.pos, true);
                    }
                }

                return ret;
            }

        };
        // ---------------------------------------------------------------------------------------------------< Private >---

        /**
         * internal next item id
         * @private
         * @type {number}
         */
        var _nextItemId = 0;

        // Fix for CQ-29112
        //
        // Caching the scene children as a a private instance variable causes problems in IE 11
        // because the HTMLCollection can go stale in that browser. Here we force the list to
        // fetch a new instance of the children collection each time.
        //
        // For performance reasons, functions that call this method should cache results at the
        // beginning of the function.
        function _getItems() {
            return this._scene.children;
        }

        /**
         * Defines the CSS rules
         * @private
         */
        function _defineCSSRules() {
            for (var i = 0; i < document.styleSheets.length; i++) {
                if (document.styleSheets[i].title === STYLE_SHEET_TITLE) {
                    // console.log('ScrollableList stylesheet present');
                    return;
                }
            }

            // create new style sheet
            // console.log('ScrollableList stylesheet missing. Creating new styles...');
            var style = document.createElement('style');
            style.setAttribute('title', STYLE_SHEET_TITLE);
            style.appendChild(document.createTextNode('')); // WebKit hack :(
            document.head.appendChild(style);

            var css = style.sheet;

            css.insertRule('.' + CSS_CLASS_WORLD + '{' +
                'width: 100%;' +
                'height: 100%;' +
                'overflow: hidden;' +
                'position: absolute;' +
                '}', 0);
            css.insertRule('.' + CSS_CLASS_SCENE + '{' +
                'width: 100%;' +
                'height: 100%;' +
                '}', 1);
            css.insertRule('.' + CSS_CLASS_ITEM_X + '{' +
                'position: absolute;' +
                'height: 100%;' +
                '}', 2);
            css.insertRule('.' + CSS_CLASS_ITEM_Y + '{' +
                'position: absolute;' +
                'width: 100%;' +
                '}', 3);
        }

        /**
         * Returns the item at the specified index or null.
         * @param {Number} idx index
         * @returns {HTMLElement} the element or null
         * @private
         */
        function _getItemAt(idx) {
            var items = _getItems.call(this);
            return idx >= 0 && idx < items.length ? items[idx] : null;
        }

        /**
         * Returns the item specified by the argument
         * @param {HTMLElement|number|string} el element
         * @returns {HTMLElement} the item or null
         * @private
         */
        function _getItem(el) {
            if (typeof el === 'number' || typeof el === 'string') {
                return _getItemAt.call(this, +el);
            }
            while (el && el.className !== this._cssItemClassName) {
                el = el.parentElement;
            }
            return el;
        }

        /**
         * initializes the debug panes
         * @private
         */
        function _initDebug() {
            if (this.options.debug) {
                var style = {
                    position: 'absolute',
                    background: 'rgba(0,0,0,0.3)',
                    fontFamily: 'monospace',
                    fontSize: '16px',
                    whiteSpace: 'pre'
                };
                var el = this.el;
                var worldX = el.offsetLeft;
                var worldW = el.clientWidth;
                var worldH = el.clientHeight;
                var dbgLeft = document.createElement('div');
                _.extend(dbgLeft.style, style, {
                    left: -worldX + 'px',
                    top: 0,
                    width: worldX + 'px',
                    height: worldH + 'px'
                });
                this._debugEl = dbgLeft;
                el.appendChild(dbgLeft);
                var dbgRight = document.createElement('div');
                _.extend(dbgRight.style, style, {
                    left: worldW + 'px',
                    right: '-1000px',
                    top: 0,
                    height: worldH + 'px'
                });
                this._world.style.overflow = 'visible';
                el.appendChild(dbgRight);
            }
        }

        /**
         * updates the debug informtions
         * @private
         */
        function _updateDebug() {
            if (this.options.debug) {
                this._debugEl.innerHTML =
                        'position: ' + this._position + '\n' +
                        '  offset: ' + this._offset + '\n' +
                        ' current: ' + this._current + '\n' +
                        '   first: ' + this._first + '\n' +
                        '    last: ' + this._last + '\n' +
                        'lastEdge: ' + this._lastEdge + '\n' +
                        '    hash: ' + this._hash + '\n' +
                        ' margin0: ' + this._marginLeft + '\n' +
                        ' margin1: ' + this._marginRight + '\n' +
                        '  nextId: ' + _nextItemId + '\n';
            }
        }

        /**
         * Adds an element to this list
         * @param {HTMLElement} element The element to add
         * @param {HTMLElement} [beforeElement] The element to position against
         * @returns {HTMLElement} new Item
         * @private
         */
        function _addElement(element, beforeElement) {
            var div = document.createElement('div');
            div.className = this._cssItemClassName;
            div.appendChild(element);
            if (beforeElement) {
                this._scene.insertBefore(div, beforeElement);
            } else {
                this._scene.appendChild(div);
            }
            return div;
        }


        /**
         * updates the item cache and recalculates the item positions and sizes if required
         * @param {boolean} forceInvalidate if true, all items' sizes will be re-initialized
         * @private
         */
        function _updateCache(forceInvalidate) {
            var _getSize = ScrollableList.FN_GET_SIZE_FOR_EL[this._direction];
            var gutter = this._gutter;
            var items = _getItems.call(this);
            var pos = 0, div;

            // calculate the caches of all items
            for (var i = 0, len = items.length; i < len; i++) {
                var item = items[i];

                // check if DOM structure is correct

                // wrap item if it's not ours
                if (item.className !== this._cssItemClassName) {
                    console.log('raw item detected');
                    div = document.createElement('div');
                    div.className = this._cssItemClassName;
                    this._scene.replaceChild(div, item);
                    div.appendChild(item);
                    i--;
                    continue;
                }

                // check if someone removed the item
                if (item.children.length === 0) {
                    console.log('removed item detected');
                    this._scene.removeChild(item);
                    i--;
                    len = items.length;
                    continue;
                }

                // check if someone added items before or after the inner parts
                if (item.children.length > 1) {
                    console.log('multiplied item detected');
                    div = document.createElement('div');
                    div.className = this._cssItemClassName;
                    div.appendChild(item.children[0]);
                    this._scene.insertBefore(div, item);
                    i--;
                    len = items.length;
                    continue;
                }

                var c;
                if (!(c = item.scrollListCache)) {
                    c = item.scrollListCache = {
                        id: _nextItemId++,
                        enabled: true, // assume new item is enabled
                        size: 0,
                        pos: 0
                    };
                }
                if (!c.size || forceInvalidate) {
                    if (!c.enabled) {
                        // need to enable the item in order to read it's size
                        c.enabled = true;
                        item.style.opacity = '';
                        item.style.display = '';
                    }
                    c.size = _getSize(item);
                }
                c.pos = pos;
                pos += c.size + gutter;
            }
            // check if there are enough elements to fill the world
            // in this case we extend it so that the scroll never snaps to the right edge
            this._lastEdge = Math.max(pos - gutter, this._worldSize);
        }

        function _reposition() {
            var _translate = _translateEl[this._direction];
            var noJitter = this._noJitter;
            var pos = 0;
            var items = _getItems.call(this);
            var first = this._first, last = this._last;
            for (var i = 0, len = items.length; i < len; i++) {
                var item = items[i];
                var cache = item.scrollListCache;
                if (i < first || i >= last) {
                    // disable item
                    if (cache.enabled) {
                        cache.enabled = false;
                        cache.tpos = 0;
                        item.style.display = 'none';
                    }
                    if (noJitter) {
                        pos += cache.size + this.options.gutter;
                    }
                } else {
                    if (!cache.enabled) {
                        cache.enabled = true;
                        item.style.display = '';
                        var el = item.firstChild;
                        if (el && el.viewController && el.viewController.layout) {
                            el.viewController.layout();
                        }
                    }
                    if (cache.tpos !== pos) {
                        _translate(item, pos);
                        cache.tpos = pos;
                    }
                    pos += cache.size + this.options.gutter;
                }
            }
        }

        function _layout() {
            var _translate = _translateEl[this._direction];

            // remember old hash and re-calc the parameters
            var hash = this._hash;
            _normalize.call(this);

            // if hash has changed, re-position the items
            if (hash !== this._hash) {
                _reposition.call(this);
            }
            // translate the scene
            _translate(this._scene, -this._offset);

            _updateDebug.call(this);

            // handle edge detection
            var deltaRight = this._position + this._edgeRight - this._lastEdge;
            if (this._position < this._edgeLeft) {
                this._edgeHit = -1;
                this._edgePosition = this._edgeLeft;
            } else if (deltaRight > 0) {
                this._edgeHit = 1;
                this._edgePosition = this._lastEdge - this._edgeRight;
            } else {
                this._edgeHit = 0;
            }
        }


        /**
         * Recalculate offsets, current item, etc, based on position.
         * @private
         */
        function _normalize() {
            var items = _getItems.call(this);
            var position = Math.round(this._position);
            var m0 = position + this._marginLeft;
            var m1 = position + this._marginRight;

            // we want that the first rendered item is always positioned at 0 relative to the scene
            var first = 0;
            var firstPos = 0;
            var lastPos = 0;
            var pos = 0;
            var current = 0;
            for (var i = 1, len = items.length; i < len; i++) {
                var cache = items[i].scrollListCache;
                pos = cache.pos;
                if (pos < m0) {
                    // item is left off-screen. so preliminary first is the next one.
                    first = i;
                    firstPos = pos;
                } else if (pos > m1) {
                    // item is right off-screen
                    break;
                }
                // calc current
                if (position - pos >= 0) {
                    current = i;
                }
                lastPos = pos;
            }
            this._first = first;
            this._last = i;
            this._offset = this._noJitter ? position : position - firstPos;
            this._current = current;

            // calculate hash string for layout to know if it needs to reposition the items
            var firstId = first < items.length ? items[first].scrollListCache.id : 'none';
            var lastId = i < items.length ? items[i].scrollListCache.id : 'none';
            this._hash = firstId + ':' + firstPos + ':' + lastId + ':' + lastPos;
        }

        return ScrollableList;

    }());

    var PhysicsModule = (function() {

        /**
         * Simple class that can apply forces to particles
         *
         * @class
         *
         * @param {Object} options Options object
         * @param {Number} [options.strength=0.001] Strength of the force
         * @param {Function} [options.forceFunction=Force.FN_LINEAR] Force function
         */
        function Force(options) {
            this.options = _.extend({}, Force.DEFAULT_OPTIONS, _.pick(options || {}, Object.keys(Force.DEFAULT_OPTIONS)));

            this.strength = this.options.strength;
            this.fn = this.options.forceFunction;
        }

        /**
         * Function that applies a force linear to the velocity
         * @param {number} v Velocity
         * @returns {number} same a 'v'
         */
        Force.FN_LINEAR = function(v) {
            return v;
        };

        /**
         * Function that applies a force quadratic to the velocity
         * @param {number} v Velocity
         * @returns {number} v^2
         */
        Force.FN_QUADRATIC = function(v) {
            return v * v;
        };

        Force.DEFAULT_OPTIONS = {
            strength: 0.001,
            forceFunction: Force.FN_LINEAR
        };

        Force.prototype = {

            /**
             * Applies the force to the given particle
             * @param {Physics.Particle} p particle to apply the force to
             */
            applyForce: function(p) {
                var f = -this.fn(p.velocity) * this.strength;
                p.applyForce(f);
            }
        };

        /**
         * Physics particle with mass == 1
         *
         * @class Physics.Particle
         * @param {Object} options configuration options
         * @param {Number} [options.velocityCap=0] max velocity that this particle allows
         * @constructor
         */
        function Particle(options) {
            this.options = _.extend({}, Particle.DEFAULT_OPTIONS, _.pick(options || {}, Object.keys(Particle.DEFAULT_OPTIONS)));

            this.velocityCap = this.options.velocityCap;
        }

        Particle.DEFAULT_OPTIONS = {
            velocityCap: 0
        };

        Particle.prototype = {

            velocityCap: 0,
            position: 0,
            velocity: 0,
            force: 0,

            /**
             * Apply a force to this particle
             * @param {Number} force Force to apply.
             */
            applyForce: function(force) {
                this.force += force;
            },

            /**
             * Update the velocity and reset the force
             * @param {Number} dt Time that passed
             */
            updateVelocity: function(dt) {
                this.velocity += this.force * dt; // assume mass == 1
                this.force = 0;
            },

            /**
             * Update the position based on it's velocity
             * @param {Number} dt Time that passed
             */
            updatePosition: function(dt) {
                var v = this.velocity;
                if (this.velocityCap) {
                    this.velocity = v =
                        v > 0 ? Math.min(this.velocityCap, v) : Math.max(-this.velocityCap, v);
                }
                this.position += dt * v;
            }
        };

        /**
         *  A force that moves a physics body to a location with a spring motion.
         *    The body can be moved to another physics body, or an anchor point.
         *
         *  @class Physics.Spring
         *  @constructor
         *  @param {Object} options options to set on drag
         */
        function Spring(options) {
            this.options = _.extend({}, Spring.DEFAULT_OPTIONS, _.pick(options || {}, Object.keys(Spring.DEFAULT_OPTIONS)));
            this.reset();
        }

        /**
         * A FENE (Finitely Extensible Nonlinear Elastic) spring force
         *      see: http://en.wikipedia.org/wiki/FENE
         * @attribute FENE
         * @param {Number} dist current distance target is from source body
         * @param {Number} rMax maximum range of influence
         * @return {Number} unscaled force
         */
        Spring.FN_FENE = function(dist, rMax) {
            var rMaxSmall = rMax * 0.99;
            var r = Math.max(Math.min(dist, rMaxSmall), -rMaxSmall);
            return r / (1 - r * r / (rMax * rMax));
        };

        /**
         * A Hookean spring force, linear in the displacement
         *      see: http://en.wikipedia.org/wiki/FENE
         * @attribute FENE
         * @param {Number} dist current distance target is from source body
         * @return {Number} unscaled force
         */
        Spring.FN_HOOK = function(dist) {
            return dist;
        };

        /**
         * @property Spring.DEFAULT_OPTIONS
         * @type Object
         * @protected
         * @static
         */
        Spring.DEFAULT_OPTIONS = {

            /**
             * The amount of time in milliseconds taken for one complete oscillation
             * when there is no damping
             *    Range : [150, Infinity]
             * @attribute period
             * @type Number
             * @default 300
             */
            period: 300,

            /**
             * The damping of the spring.
             *    Range : [0, 1]
             *    0 = no damping, and the spring will oscillate forever
             *    1 = critically damped (the spring will never oscillate)
             * @attribute dampingRatio
             * @type Number
             * @default 0.1
             */
            dampingRatio: 0.1,

            /**
             * The rest length of the spring
             *    Range : [0, Infinity]
             * @attribute length
             * @type Number
             * @default 0
             */
            length: 0,

            /**
             * The maximum length of the spring (for a FENE spring)
             *    Range : [0, Infinity]
             * @attribute length
             * @type Number
             * @default Infinity
             */
            maxLength: Infinity,

            /**
             * The location of the spring's anchor, if not another physics body
             *
             * @attribute anchor
             * @type Number
             * @optional
             */
            anchor: 0,

            /**
             * The type of spring force
             * @attribute forceFunction
             * @type Function
             */
            forceFunction: Spring.FN_HOOK
        };

        Spring.prototype = {

            /**
             * Resets this spring.
             */
            reset: function() {
                var opts = this.options;
                this._energy = 0;
                this._fn = opts.forceFunction;
                this._stiffness = Math.pow(2 * Math.PI / opts.period, 2);
                this._damping = 4 * Math.PI * opts.dampingRatio / opts.period;
                this._anchor = opts.anchor;
                this._restLength = opts.length;
                this._maxLength = opts.maxLength;
            },

            /**
             * Sets new options and resets this spring.
             * @param {Object} options The new options
             */
            setOptions: function(options) {
                _.extend(this.options, options);
                this.reset();
            },


            /**
             * Apply this springs force to the give particle
             * @param {Physics.Particle} p Particle to apply the force to
             */
            applyForce: function applyForce(p) {
                var stiffness = this._stiffness;

                // distance
                var dist = this._anchor - p.position - this._restLength;

                // apply spring function
                var force = stiffness * this._fn(dist, this._maxLength);

                // damp force
                force -= p.velocity * this._damping;

                p.applyForce(force);

                // update energy
                this._energy = 0.5 * stiffness * dist * dist;
            }
        };

        return {
            Force: Force,
            Particle: Particle,
            Spring: Spring
        };

    }());

    window.ScreensScroller = (function(ScrollableList, P) {

        /**
         * Event reporting that the scroll view 'snapped' to an item
         *
         * @event module:scroll/ScrollView#scroll-snap
         * @type {object}
         * @property {number} idx - The index of the item that snapped
         */

        // spring attachment types
        var SPRING_BODY_NONE = 0;
        var SPRING_BODY_EDGE = 1;
        var SPRING_BODY_SNAP = 2;

        // Time in milliseconds allowed for a frame to render.
        var TIME_BASED_ANIMATION_THRESHOLD = 33.3;

        /**
         * The scroll view connects the mouse/pointer input to a scrollable list and adds the physics.
         *
         * @class ScrollView
         * @constructor
         * @param {HTMLElement|String} el The HTML element to scroll
         * @param {Object} [options] An object of configurable options.
         * @param {Number} [options.direction=ScrollView.DIRECTION_X] The direction of the list.
         * @param {Object} [options.scrollableList] Options passed to the scrollable list
         * @param {Function} [options.panDelegate] delegate for pan gesture
         */
        function ScrollView(el, options) {
            this.el = el = el.jquery ? el[0] : el;
            this.options = _.extend({}, ScrollView.DEFAULT_OPTIONS, _.pick(options || {}, Object.keys(ScrollView.DEFAULT_OPTIONS)));

            this._direction = this.options.direction ? 1 : 0; // normalize
            var listOptions = this.options.scrollableList;
            listOptions.direction = this._direction; // force same direction option

            // check for edge margins
            if (!listOptions.edgeMargin) {
                listOptions.edgeMargin = this.options.edgeMargin;
            }
            this.list = new ScrollableList(el, listOptions);

            // init internal states

            /** enable/disable the entire scrollview */
            this._enabled = true;

            this._simulating = false;
            this._onEdge = false;
            this._snapCheckPending = false;
            this._recheck = false; // used to trigger edge/snap-check after an item was removed
            this._springBody = SPRING_BODY_NONE;
            this._springPosition = 0;

            this._watchedElements = [];

            // information about the last touch event
            this._lastTouch = null;

            // init physics
            this._particle = new P.Particle({
                velocityCap: this.options.speedLimit
            });

            this._drag = new P.Force({
                forceFunction: P.Force.FN_QUADRATIC,
                strength: this.options.drag
            });

            this._friction = new P.Force({
                forceFunction: P.Force.FN_LINEAR,
                strength: this.options.friction

            });

            this._springOpts = [];
            this._springOpts[SPRING_BODY_NONE] = {};
            this._springOpts[SPRING_BODY_EDGE] = {
                period: this.options.edgePeriod,
                dampingRatio: this.options.edgeDamp
            };
            this._springOpts[SPRING_BODY_SNAP] = {
                period: this.options.snapPeriod,
                dampingRatio: this.options.snapDamp
            };
            this._spring = new P.Spring(this._springOpts[SPRING_BODY_EDGE]);

            // init event listener
            if (this.options.panGestureEnabled) {
                this._sistine = new Sistine.Manager(el, {
                    recognizers: [
                        new Sistine.Pan({
                            eventName: 'pan',
                            direction: this._direction ? Sistine.DIRECTION_VERTICAL : Sistine.DIRECTION_HORIZONTAL,
                            delegate: this.options.panDelegate,
                            minLockDistance: 0.0,
                            minPointers: this.options.minPointers,
                            maxPointers: this.options.maxPointers
                        }),
                        new Sistine.Tap({
                          eventName: 'tap',
                          taps: 1
                        })
                    ]
                });
                this._sistine.on('pan', _handlePan.bind(this));
                this._sistine.on('tap', _handleTap.bind(this));
            }
        }

        /**
         * X direction (horizontal)
         *
         * @alias ScrollView.DIRECTION_X
         * @type Number
         * @static
         */
        ScrollView.DIRECTION_X = 0;

        /**
         * Y direction (vertical)
         *
         * @alias ScrollView.DIRECTION_Y
         * @type Number
         * @static
         */
        ScrollView.DIRECTION_Y = 1;

        /**
         * Default Options
         *
         * @constant {Object}
         */
        ScrollView.DEFAULT_OPTIONS = {

            /**
             * Direction of the scroll
             */
            direction: ScrollView.DIRECTION_X,

            /**
             * Options passed to the ScrollableList
             */
            scrollableList: {},

            /**
             * Friction of the particle
             */
            friction: 0.005,

            /**
             * Drag, opposing the velocity of the particle
             */
            drag: 0.0001,

            /**
             * Period of the edge spring
             */
            edgePeriod: 300,

            /**
             * damping of the edge spring.
             */
            edgeDamp: 1,

            /**
             * the lower and upper margins of the edges. suitable
             * to place the edge springs outside the world, eg. to add
             * "bumpers" around the list
             */
            edgeMargin: [0, 0],

            /**
             * Controls if the edge spring can be stretched beyond physically possible while dragging.
             * I.e. if the edge spring is enabled during dragging or not.
             */
            edgeResist: true,

            /**
             * Overall speed limit of the particle
             */
            speedLimit: 10,

            /**
             * enable/disable snap
             */
            snap: false,

            /**
             * period of the snap spring
             */
            snapPeriod: 500,

            /**
             * damping of the snap spring.
             */
            snapDamp: 0.8,

            /**
             * Velocity below which the scollview will enable snapping
             */
            snapSpeedThreshold: 1,

            /**
             * Velocity above which the scollview will snap to the next item
             */
            snapNextSpeed: 0.1,

            /**
             * delegate for the pan gesture
             */
            panDelegate: null,
            onTapHandler: null,

            /**
             * ability to disable the pan gesture if needed
             */
            panGestureEnabled: true,

            /**
             * Minimum number of pointers to be recognised for the swipe
             * @type {Number}
             */
            minPointers: 1,

            /**
             * Maximum number of pointers to be recognised for the swipe
             * @type {Number}
             */
            maxPointers: 1
        };

        function _delegateToList(fn) {
            return function() {
                return fn.apply(this.list, arguments);
            };
        }

        ScrollView.prototype = {

            /**
             * Destroys this scroll view and releases all resources.
             */
            destroy: function() {
                if (this._sistine) {
                    this._sistine.off('pan');
                    this._sistine.destroy();
                    this._sistine = null;
                }
                this.list.destroy();
                this.list = null;
            },

            /**
             * Returns the current scroll position
             * @returns {number} position in pixels
             */
            getPosition: function() {
                return this._particle.position;
            },

            /**
             * Sets the current scroll position and updates the scroll view
             * @param {number} pos position
             * @return {number} the effective position
             */
            setPosition: function(pos) {
                // don't allow to position the list outside the edges
                pos = this.list.limitPosition(pos);
                this._particle.position = pos;

                // ensure that spring is disabled, otherwise it could snap back
                this._springBody = SPRING_BODY_NONE;

                // ok here?
                _tick.call(this, 0);
                return pos;
            },

            /**
             * Scrolls to the given element. Currently not animated.
             * @param {HTMLElement|number} elem Element or index
             */
            scrollTo: function(elem) {
                var pos = this.positionOf(elem);
                this.setPosition(pos);
            },

            /**
             * Sets a new position based on the delta.
             * @param {number} delta to add to the position
             * @return {number} the effective delta
             */
            incPosition: function(delta) {
                var pos = this.getPosition();
                var newPos = this.setPosition(pos + delta);
                return newPos - pos;
            },

            /**
             * Watch and adjust scrollview
             * @todo: improve semantics
             * @param {HTMLElement} el The element to watch
             * @return {ScrollView~WatchInfo} the watch info
             */
            watchAndAdjust: function(el) {
                function adjust(watch) {
                    var delta = watch.size - watch.oldSize;
                    this.setPosition(this.getPosition() + delta / 2);
                    return true;
                }
                // ensure we got the item
                return _registerWatch.call(this, el, adjust, this);
            },

            // delegated list functions. todo: copy jsDoc

            invalidate: function() {
                this.list.invalidate.apply(this.list, arguments);

                if (this._recheck) {
                    this._recheck = false;
                    // simulate a bit in case items were removed or inserted.
                    _tick.call(this, 1000 / 60);
                    this._snapCheckPending = true;
                    _start.call(this);
                }
            },

            invalidateAll: function() {
                this.list.invalidateAll.apply(this.list, arguments);

                if (this._recheck) {
                    this._recheck = false;
                    // simulate a bit in case items were removed or inserted.
                    _tick.call(this, 1000 / 60);
                    this._snapCheckPending = true;
                    _start.call(this);
                }
            },

            /**
             * The splice() method changes the content of this list, adding new elements while removing old elements.
             *
             * @param {number} [start=0]
             *                       Index at which to start changing the array. If greater than the length of the array,
             *                       actual starting index will be set to the length of the array.
             *                       If negative, will begin that many elements from the end.
             * @param {number} [deleteCount='all']
             *                       An integer indicating the number of old array elements to remove. If deleteCount is 0,
             *                       no elements are removed. In this case, you should specify at least one new element.
             *                       If deleteCount is greater than the number of elements left in the array starting at
             *                       index, then all of the elements through the end of the array will be deleted.
             *                       If deleteCount is undefined, all elements after start are removed.
             * @param {HTMLElement|HTMLElement[]} [elements]
             *                       The elements to add to the list. If you don't specify any elements,
             *                       splice simply removes elements from the array.
             *
             * @example
             *   splice()                          - removes all elements
             *   splice(item)                      - inserts one item at the beginning
             *   splice(Number.MAX_VALUE, 0, item) - inserts one item at the end
             *   splice(-1, 1, item)               - replaces the last element
             *
             * @returns {HTMLElement[]}
             *                       An array containing the removed elements.
             *                       If only one element is removed, an array of one element is returned.
             *                       If no elements are removed, an empty array is returned.
             *
             */
            splice: function(start, deleteCount, elements) {
                var oldPos = this.list.getPosition();
                var ret = this.list.splice.apply(this.list, arguments);
                this._recheck = _.isNumber(deleteCount) && deleteCount > 0;

                // reset particles position in case list changed it does to a insert or removal
                // before the 'current' one.
                var newPos = this.list.getPosition();
                if (newPos !== oldPos) {
                    this.setPosition(newPos);
                }
                return ret;
            },

            handlePan: function(e) {
                _handlePan.call(this, e);
            },

            length: _delegateToList(ScrollableList.prototype.length),
            indexOf: _delegateToList(ScrollableList.prototype.indexOf),
            at: _delegateToList(ScrollableList.prototype.at),
            positionOf: _delegateToList(ScrollableList.prototype.positionOf),
            recalculateSize: _delegateToList(ScrollableList.prototype.recalculateSize),
            children: _delegateToList(ScrollableList.prototype.children)

        };

        // -----------------------------------------------------------------------------------------------------------------
        // Private

        var _now = _.now;

        /**
         * Handles the pan event
         * @param {Sistine.Event} e Event that triggers the pan.
         *
         * @alias ScrollView#_handlePan
         * @private
         */
        function _handlePan(e) {
            if (!this._enabled) {
                return;
            }
            if (e.state === Sistine.STATE_STARTED) {
                _handleStart.call(this, e);
            } else if (e.state === Sistine.STATE_CHANGED) {
                _handleMove.call(this, e);
            } else if (e.state === Sistine.STATE_ENDED) {
                _handleEnd.call(this, e);
            }
        }

        function _handleTap(e) {
            if (this.options.onTapHandler) {
                this.options.onTapHandler(e);
            }
        }

        /**
         * Handles the start of a touch or mouse interaction
         * @param {Sistine.Event} e Event that triggers the start.
         *
         * @alias ScrollView#_handleStart
         * @private
         */
        function _handleStart(e) {
            if (this._lastTouch) {
                return;
            }
            // previous point (based on pointer) for delta calculation
            this._lastTouch = {
                id: e.pointer.id,
                idSet: e.pointerSet.keys.slice(0),
                t: _now(),
                p: e.pointer.position(),
                v: [0, 0],
                d: [0, 0]
            };

            // we stop simulation at this point because we update the physics on move.
            this._springBody = SPRING_BODY_NONE;
            this._onEdge = false;
            _stop.call(this);
        }

        /**
         * updates the touch info parameters based on the event and current time
         *
         * @param {Object}         info The touch info
         * @param {Sistine.Event}  e    The event that was triggered
         * @static
         * @private
         */
        function _updateLastTouchInfo(info, e) {
            var t = _now();
            var tp = info.tp = info.t;
            var dt = info.dt = t - tp;    // dt = t - t'
            var p = e.pointer.position(); // p = event.position
            var d;
            for (var i = 0; i < 2; i++) {
                d = info.d[i] = p[i] - info.p[i];     // delta = p - p'
                info.v[i] = !dt ? info.v[i] : d / dt; // v = delta / dt
            }
            info.p = p;
            info.t = t;
        }

        /**
         * Handles the touch or mouse move
         *
         * @param {Sistine.Event} e The event that was triggered
         *
         * @alias ScrollView#_handleMove
         * @private
         */
        function _handleMove(e) {
            if (!this._lastTouch || this._lastTouch.idSet.indexOf(e.pointer.id) === -1) {
                return;
            }
            _updateLastTouchInfo(this._lastTouch, e);

            // set velocity to 0 while moving scroll around
            this._particle.velocity = 0;
            this._particle.position -= this._lastTouch.d[this._direction];

            // trigger engine update which in turn sets the scroll list position
            var dt = 0;
            if (this.options.edgeResist) {
                dt = this._lastTouch.dt;
            }
            _tick.call(this, dt);
        }

        /**
         * Handles the end of the touch or mouse interaction
         * @param {Sistine.Event} e The event that was triggered
         *
         * @alias ScrollView#_handleEnd
         * @private
         */
        function _handleEnd(e) {
            if (!this._lastTouch || this._lastTouch.idSet.indexOf(e.pointer.id) === -1) {
                return;
            }

            // set velocity and position to the one of the touch
            this._particle.velocity -= this._lastTouch.v[this._direction];
            this._particle.position -= this._lastTouch.d[this._direction];

            // check if we need to snap to the next or previous item
            this._snapCheckPending = true;

            // start physics simulation
            _start.call(this);

            // remove event listeners
            this._lastTouch = null;
        }

        /**
         * Check if we're on/off an edge
         *
         * @alias ScrollView#_checkEdge
         * @private
         */
        function _checkEdge() {
            var edgeHit = !!this.list._edgeHit;
            if (!this._onEdge && edgeHit) {
                _enableSpring.call(this, SPRING_BODY_EDGE, this.list._edgePosition);

            } else if (this._onEdge && !edgeHit && this._springBody !== SPRING_BODY_SNAP) {
                // disable edge spring but only if particle is barely moving
                if (this._springBody && Math.abs(this._particle.velocity) < 0.01) {
                    _disableSpring.call(this);
                }
            }
            this._onEdge = edgeHit;
        }

        /**
         * check if snapping is enabled and if we need to check for the scroll to snap
         *
         * @alias ScrollView#_checkSnap
         * @private
         */
        function _checkSnap() {
            if (!this._snapCheckPending || !this.options.snap) {
                return;
            }

            // check if the velocity is low enough to snap
            var v = Math.abs(this._particle.velocity);
            if (v > this.options.snapSpeedThreshold) {
                return;
            }
            this._snapCheckPending = false;

            // get current info
            var info = this.list.infoOf(this.list.getCurrentIdx());
            if (!info) {
                return;
            }

            // don't snap if we are already on an edge
            if (this._springBody === SPRING_BODY_EDGE) {
                _dispatchEvent.call(this, 'scroll-snap', {
                    idx: info.idx,
                    view: this
                });
                return;
            }

            var pos = info.position;
            var snapIdx = info.idx;

            // test if the velocity is big enough to trigger switching to the next item
            var shouldSnapNext = v > this.options.snapNextSpeed;

            // test if the current position is greater than the half distance
            var closeToNext = this._particle.position > (info.position + info.nextPos) / 2;

            // test if the velocity points in the direction of the next item
            var directionToNext = this._particle.velocity > 0;

            // if either the velocity is right or the position, snap to the next item
            if ((shouldSnapNext && directionToNext) || (!shouldSnapNext && closeToNext)) {
                pos = info.nextPos;
                snapIdx++;
            }

            _dispatchEvent.call(this, 'scroll-snap', {
                idx: snapIdx,
                view: this
            });

            _enableSpring.call(this, SPRING_BODY_SNAP, pos);
        }

        /**
         * Dispatches an event on {code}this.el{code}.
         *
         * @alias ScrollView#_dispatchEvent
         * @private
         *
         * @param {String} type event type or name
         * @param {Object} [data] additional event data added to event.detail.
         */
        function _dispatchEvent(type, data) {
            var ev = document.createEvent('Event');
            ev.initEvent(type, true, true);
            ev.detail = data || {};
            ev.viewController = this;
            this.el.dispatchEvent(ev);
        }


        /**
         * Enables the spring if not enabled already.
         *
         * @param {Integer} body    the spring attachment type
         * @param {Integer} pos     the position
         *
         * @alias ScrollView#_enableSpring
         * @private
         */
        function _enableSpring(body, pos) {
            if (this._springBody === SPRING_BODY_NONE) {
                this._springBody = body;
                var opts = _.extend({}, this._springOpts[body], {
                    anchor: pos
                });
                this._spring.setOptions(opts);
                this._springPosition = pos;
            }
        }

        /**
         * Disables the spring if not disabled already
         *
         * @alias ScrollView#_disableSpring
         * @private
         */
        function _disableSpring() {
            if (this._springBody) {
                this._springBody = SPRING_BODY_NONE;
                // ensure we snap to the edge
                this._particle.position = this._springPosition;
                // and tick
                _tick.call(this, 0);
            }
        }

        /**
         * moves the world a bit
         * @param {number} dt milliseconds to tick
         *
         * @alias ScrollView#_tick
         * @private
         */
        function _tick(dt) {
            // check if we need to snap to an item
            _checkSnap.call(this);

            if (dt === null) {
                return;
            }

            var isDeltatimeAcceptable = true;

            if (dt > TIME_BASED_ANIMATION_THRESHOLD) {
                dt = TIME_BASED_ANIMATION_THRESHOLD;
                isDeltatimeAcceptable = false;
            }

            // apply forces
            var p = this._particle;
            if (this._springBody) {
                this._spring.applyForce(p);
            } else {
                this._drag.applyForce(p);
                this._friction.applyForce(p);
            }
            // Update virtual world.
            p.updateVelocity(dt);
            p.updatePosition(dt);

            // Update real world. But only if cpu is fast enough. Otherwise skip.
            if (isDeltatimeAcceptable) {
                this.list.setPosition(p.position, true);
            }

            // check if the list is on an edge
            _checkEdge.call(this);

        }


        /**
         * Info object returned by watch methods.
         *
         * @name ScrollView~WatchInfo
         * @param {HTMLElement} el
         * @param {ScrollView~FN_WATCH_CB} fn
         * @param {number} oldSize
         * @param {number} size
         * @param {object} scope
         * @param {number} lastChange
         * @param {number} timeout
         */

        /**
         * Callback function for watch methods
         *
         * @callback ScrollView~FN_WATCH_CB
         * @param {ScrollView~WatchInfo} watchInfo
         * @this ScrollView~WatchInfo.scope or ScrollView~WatchInfo.el
         * @returns {boolean} true if the element should be invalidated
         * @private
         */

        /**
         * Registers a watch on the given element.
         *
         * @alias ScrollView#_registerWatch
         * @param {HTMLElement}             el          The element
         * @param {ScrollView~FN_WATCH_CB}  fn          The callback function
         * @param {object}                  [scope=el]  Callback scope (this)
         *
         * @return {ScrollView~WatchInfo} the watch info or null if it was not registered
         * @private
         */
        function _registerWatch(el, fn, scope) {
            // check if watch does not exist yet for element
            var watchList = this._watchedElements;
            for (var i = 0; i < watchList.length; i++) {
                if (watchList[i].el === el) {
                    return null;
                }
            }

            var size = ScrollableList.FN_GET_SIZE_FOR_EL[this._direction](el);
            var watch = {
                el: el,
                fn: fn,
                oldSize: size,
                size: size,
                scope: scope,
                lastChange: _now(),
                timeout: 500
            };
            watchList.push(watch);
            _watch.call(this);
            return watch;
        }

        function _watch() {
            var _getSize = ScrollableList.FN_GET_SIZE_FOR_EL[this._direction];
            var self = this;
            var watchList = this._watchedElements;
            function run() {
                var invalidate = [];
                for (var i = 0; i < watchList.length; i++) {
                    var watch = watchList[i];
                    var el = watch.el;
                    // check if element still exists in dom
                    if (!document.body.contains(el)) {
                        // element was removed, but scroll wasn't completely adjusted
                        if (watch.size > 0) {
                            watch.oldSize = watch.size;
                            watch.size = 0;
                            watch.fn.call(watch.scope || el, watch);
                        }
                        watchList.splice(i--, 1);
                        continue;
                    }
                    var now = _now();
                    var newSize = _getSize(el);
                    if (watch.size !== newSize) {
                        watch.oldSize = watch.size;
                        watch.size = newSize;
                        watch.lastChange = now;
                        if (watch.fn.call(watch.scope || el, watch)) {
                            invalidate.push(el);
                        }
                    } else {
                        // check timeout
                        var dt = now - watch.lastChange;
                        if (dt > watch.timeout) {
                            watchList.splice(i--, 1);
                        }
                    }
                }
                if (invalidate.length > 0) {
                    self.list.invalidate.apply(self.list, invalidate);
                }

                if (watchList.length) {
                    requestAnimationFrame(run);
                }
            }

            // only start if watchlist is not empty
            if (watchList.length) {
                requestAnimationFrame(run);
            }
        }

        /**
         * Starts the physics simulation
         *
         * @alias ScrollView#_start
         * @private
         */
        function _start() {
            var self = this, tp;

            function run() {
                if (!self._simulating) {
                    // abort simulation when done.
                    return;
                }
                var t = _now();
                var dt = Math.max(1, t - tp);
                tp = t;

                _tick.call(self, dt);

                // check if we can stop the animation.
                if (!self._snapCheckPending && Math.abs(self._particle.velocity) < 0.05) {
                    if (!self._springBody || self._spring._energy < 0.0001) {
                        _disableSpring.call(self);
                        _stop.call(self);
                        return;
                    }
                }
                requestAnimationFrame(run);
            }

            if (!this._simulating) {
                this._simulating = true;
                tp = _now();
                requestAnimationFrame(run);
            }
        }

        /**
         * Stops the physics simulation
         *
         * @alias ScrollView#_stop
         * @private
         */
        function _stop() {
            if (this._simulating) {
                this._simulating = false;
            }
        }

        // module export
        return ScrollView;

    }(ScrollableListModule, PhysicsModule));

}(window.jQuery, window._));
