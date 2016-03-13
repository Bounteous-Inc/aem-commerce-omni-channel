function productPageSwitcher_init($) {
    'use strict';

    var IMG_COLOR_SELECTOR = '.js-Color-selector';
    var IMG_CONTAINER = '.wr-ProductDetail-image';

    $(document).on('click', IMG_COLOR_SELECTOR, function(ev) {
        var target = ev.currentTarget.dataset.target;

        if (target) {
            $(IMG_CONTAINER).load(target + '.html');
        }
    });
}

productPageSwitcher_init(window.$);
