"use strict";

var global = this;

use(function () {

    var rootPage = currentPage.getAbsoluteParent(1);	// /content/wetelco
    var level1Page = currentPage.getAbsoluteParent(2);	// /content/wetelco/<language>
    var level2Page = currentPage.getAbsoluteParent(3);	// /content/wetelco/<language>/personal...
    var level3Page = currentPage.getAbsoluteParent(4);
    var level4Page = currentPage.getAbsoluteParent(5);

    return {
        rootPage: rootPage,
        level1Page: level1Page,
        level2Page: level2Page,
        level3Page: level3Page,
        level4Page: level4Page
    };
});
