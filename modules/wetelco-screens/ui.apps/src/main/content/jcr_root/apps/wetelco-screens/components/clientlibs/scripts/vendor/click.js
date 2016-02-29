(function () {

    if (!window.top.cordova) {
        return;
    }

    var CLICK_THRESHOLD = 20;
    var CLICK_TIMEOUT = 200;

    function getTouches(event) {
        return (event.originalEvent && event.originalEvent.touches) ? event.originalEvent.touches.length > 0 ?
            $.extend(true, {}, event.originalEvent.touches) :
            (event.originalEvent.changedTouches.length > 0 ?
                $.extend(true, {}, event.originalEvent.changedTouches) :
                []
            ) : [];
    }

    function getTarget(event) {
        return event.originalEvent.target;
    }

    function Click() {
        this.complete = false;
    }

    Click.prototype.start = function(ev) {
        if (this.complete) {
            return;
        }

        this.startTouches = getTouches(ev);
        this.target = getTarget(ev);

        this._timeout = setTimeout(function () {
            if (!this.cancelled) {

            }
        }, CLICK_TIMEOUT);
    };

    Click.prototype.end = function(ev) {
        this.endTouches = getTouches(ev);
        this.endTarget = getTarget(ev);

        // check same amount of pointers
        if (this.startTouches.length !== this.endTouches.length) {
            this.cancel();
        }

        // check if touch moved too much
        var deltaX = this.startTouches.pageX - this.endTouches.pageX;
        var deltaY = this.startTouches.pageY - this.endTouches.pageY;

        var delta = Math.sqrt(Math.exp(deltaX, 2) + Math.exp(deltaY, 2));

        if (delta > CLICK_THRESHOLD) {
            this.cancel();
        }

        var newEv = $.extend($.Event('click'), {
            which: 1,
            clientX: ev.clientX,
            clientY: ev.clientY,
            pageX: ev.pageX,
            pageY: ev.pageY,
            screenX: ev.screenX,
            screenY: ev.screenY,
            originalEvent: ev.originalEvent
        });

        $(this.target).trigger(newEv);
    };

    Click.prototype.cancel = function() {
        this.completed = true;
        this.cleanup();
    };

    Click.prototype.cleanup = function() {
        this.clearTimeout(this._timeout);
        this._timeout = null;
        this.startTouches = null;
        this.endTouches = null;
        this.target = null;
    };

    $(document).on('touchstart', function (ev) {
        var ts = (new Date()).getTime();
        var c = new Click();

        c.start(ev);

        $(document).on('touchend.' + ts + ' touchcancel.' + ts, function () {
            $(document).off('touchend.' + ts + ' touchcancel.' + ts);
            c.end(ev);

            c = null;
        });
    });

}());