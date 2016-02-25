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
$CQ(document).ready(function() {
    var doc = $CQ(document);
    var wrappingEditorDialog = $CQ('<div/>', {'title': 'Edit gift wrapping'}).appendTo('body');
    wrappingEditorDialog.dialog({
        autoOpen: false,
        modal: true,
        height: 210,
        width: 270,
        zIndex: 90000
    });
    doc.on("click", ".edit-wrapping-link", function(e) {
        e.preventDefault();
        var html = $CQ(".cartEntryProperties-wrapping-form", e.target)[0].cloneNode(true);
        $CQ(html).on("change", ":checkbox", function(e) {
            if (!$CQ(":checkbox", html)[0].checked) {
                $CQ(":text", html)[0].value = "";
            }
        });
        wrappingEditorDialog.dialog("option", "position", { my: "left center", at: "left center", collision: "fit", of: e.target } );
        wrappingEditorDialog.html(html).dialog("open");
    });
});
