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
 * @class Ejst.InsertTextPlugin
 * @extends CQ.form.rte.plugins.Plugin
 * This is a custom RTE Plug-in based on {@link CQ.form.rte.plugins.Plugin}.
 * Clicking the toolbar button opens a dialog that allows inserting a text.
 * @constructor
 * Creates a new InsertTextPlugin.
 * @param {Object} config The config object
 */
Ejst.InsertTextPlugin = CQ.Ext.extend(CQ.form.rte.plugins.Plugin, {

    /**
     * @private
     */
    insertTextUI: null,

    /**
     * @private
     */
    insertTextDialog: null,

    /**
     * @private
     */
    savedRange: null,

    constructor: function(editorKernel) {
        Ejst.InsertTextPlugin.superclass.constructor.call(this, editorKernel);
    },

    getFeatures: function() {
        return [ "inserttext" ];
    },

    initializeUI: function(tbGenerator) {
        var plg = CQ.form.rte.plugins;
        var ui = CQ.form.rte.ui;
        if (this.isFeatureEnabled("inserttext")) {
            this.insertTextUI = new ui.TbElement("inserttext", this, false,
                    this.getTooltip("inserttext"));
            tbGenerator.addElement("misc", plg.Plugin.SORT_MISC, this.insertTextUI, 200);
        }
    },

    /**
     * Inserts a text using the corresponding dialog.
     * @private
     */
    insertText: function(context) {
        if (!this.insertTextDialog) {
            var dialogConfig = this.config.dialogConfig || { };
            this.insertTextDialog = new Ejst.InsertTextPlugin.Dialog(dialogConfig);
        }
        this.insertTextDialog.editContext = context;
        this.insertTextDialog.plugin = this;
        if (CQ.Ext.isIE) {
            this.savedRange = context.doc.selection.createRange();
        }
        this.insertTextDialog.setPosition(this.editorKernel.calculateWindowPosition());
        this.insertTextDialog.show();
        window.setTimeout(function() {
            this.insertTextDialog.toFront();
        }.createDelegate(this), 10);
    },

    executeInsertText: function(context, textToInsert) {
        if (CQ.Ext.isIE) {
            this.savedRange.select();
        }
        this.editorKernel.relayCmd("InsertHTML", "[" + textToInsert + "]");
    },

    notifyPluginConfig: function(pluginConfig) {
        // configuring "special characters" dialog
        pluginConfig = pluginConfig || { };
        var defaults = {
            "tooltips": {
                "inserttext": {
                    "title": CQ.I18n.getMessage("Insert a text"),
                    "text": CQ.I18n.getMessage("Inserts a predefined text from a different source.")
                }
            }
        };
        CQ.Util.applyDefaults(pluginConfig, defaults);
        this.config = pluginConfig;
    },

    execute: function(id, value, options) {
        var context = options.editContext;
        if (id == "inserttext") {
            this.insertText(context);
        }
    },

});


// register plugin
CQ.form.rte.plugins.PluginRegistry.register("inserttext", Ejst.InsertTextPlugin);


Ejst.InsertTextPlugin.Dialog = CQ.Ext.extend(CQ.Ext.Window, {

    constructor: function(config) {
        config = config || { };
        var dialogRef = this;
        var defaults = {
            "title": CQ.I18n.getMessage("Reference text"),
            "modal": true,
            "width": 400,
            "height": 160,
            "layout": "fit",
            "items": [ {
                    "xtype": "panel",
                    "layout": "fit",
                    "bodyStyle": "overflow: auto;",
                    "stateful": false,
                    "items": [ {
                            "border": false,
                            "xtype": "form",
                            "stateful": false,
                            "items": [ {
	                                "itemId": "linkedText",
	                                "name": "linkedText",
	                                "anchor": CQ.themes.Dialog.ANCHOR,
	                                "fieldLabel": CQ.I18n.getMessage("Path to text:"),
	                                "xtype": "textfield"
	                            }
	                        ],
                            "afterrender": function() {
                                this.body.addClass("cq-rte-basewindow");
                                dialogRef.dialogItems = this.items;
                                dialogRef.form = this.getForm();
                            }
                        }
                    ]
                }
            ],
            "buttons": [ {
                    "itemId": "okButton",
                    "name": "okButton",
                    "text": CQ.I18n.getMessage("OK"),
                    "handler": this.apply,
                    "disabled": false,
                    "scope": this
                }, {
                    "itemId": "cancelButton",
                    "name": "cancelButton",
                    "text": CQ.I18n.getMessage("Cancel"),
                    "handler": this.cancel,
                    "disabled": false,
                    "scope": this
                }
            ]            
        };
        CQ.Util.applyDefaults(config, defaults);
        Ejst.InsertTextPlugin.Dialog.superclass.constructor.call(this, config);
    },
    
    apply: function() {
        var text = this.items.items[0].items.items[0].items.get("linkedText").getValue();
        this.hide();
        this.plugin.executeInsertText(this.editContext, text);
    },
    
    cancel: function() {
        this.hide();
        this.plugin.editorKernel.deferFocus();
    }
    
});