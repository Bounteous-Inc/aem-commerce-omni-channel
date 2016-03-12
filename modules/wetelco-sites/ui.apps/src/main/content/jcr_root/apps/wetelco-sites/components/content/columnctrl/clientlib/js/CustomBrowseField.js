/*
 * Copyright 1997-2010 Day Management AG
 * Barfuesserplatz 6, 4001 Basel, Switzerland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Day Management AG, ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Day.
 */

/**
 * @class Ejst.CustomBrowseField
 * @extends CQ.Ext.form.TriggerField
 * This is a custom widget based on {@link CQ.Ext.form.TriggerField}.
 * Clicking the trigger opens a window with a tree.
 * @constructor
 * Creates a new CustomBrowseField.
 * @param {Object} config The config object
 */
Ejst.CustomBrowseField = CQ.Ext.extend(CQ.Ext.form.TriggerField, {

    browseWindow: null,

    // overriding CQ.Ext.form.TriggerField#onTriggerClick
    onTriggerClick: function(evt) {
        this.getBrowseWindow().show();
    },

    // private
    getBrowseWindow: function() {
        var browseField = this;
        // tree config
        var treePanel = new CQ.Ext.tree.TreePanel({
            border:false,
            loader: {
                dataUrl:CQ.HTTP.externalize("/bin/wcm/siteadmin/tree.json"),
                requestMethod:"GET",
                baseParams: {
                    "_charset_": "utf-8"
                },
                listeners: {
                    beforeload: function(loader, node) {
                        this.baseParams.path = node.getPath();
                    }
                }
            },
            root: {
                nodeType:"async",
                text:CQ.I18n.getMessage("ExtJS Training"),
                name:"apps/extjstraining",
                expanded:true
            }
        });
        var win = new CQ.Ext.Window({
            cls:"ejst-browsewindow",
            width:300,
            height:400,
            items: treePanel,
            bodyStyle:"padding:10px;background-color:white",
            closeAction:"hide",
            title:CQ.I18n.getMessage("Select a path"),
            buttons: [{
                text:CQ.I18n.getMessage("OK"),
                handler: function() {
                    // pass selected path from window to browse field on OK
                    var path = treePanel.getSelectionModel().getSelectedNode().getPath();
                    browseField.setValue(path);
                    win.hide();
                }
            }]
        });
        // anchor window to browse field
        win.on("render", function() {
           this.anchorTo(browseField.getEl(), "tl", [0, -100]);
        });
        // pass selected path from browse field to window on show
        win.on("beforeshow", function(browse) {
            var path = this.getValue();
            var treePanel = browse.items.get(0);
            if (path) {
                treePanel.selectPath(path);
            } else {
                treePanel.getRootNode().select();
            }
        }, browseField);

        return win;
    }

});

// register xtype
CQ.Ext.reg('ejstbrowse', Ejst.CustomBrowseField);
