/*
 * Copyright 1997-2009 Day Management AG
 * Barfuesserplatz 6, 4001 Basel, Switzerland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Day Management AG, ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Day.
 */

//------------------------------------------------------------------------------
// Exercise 1: Dialog Basics
//------------------------------------------------------------------------------
Ejst.x1 = {};

//------------------------------------------------------------------------------
// Exercise 2: Dynamic Dialogs
//------------------------------------------------------------------------------
Ejst.x2 = {};

/**
 * Manages the tabs of the specified tab panel. The tab with
 * the specified ID will be shown, the others are hidden.
 * @param {CQ.Ext.TabPanel} tabPanel The tab panel
 * @param {String} tab the ID of the tab to show
 */
Ejst.x2.manageTabs = function(tabPanel, tab) {
    var tabs=['selection','tab1','tab2','tab3'];
    var index = tab ? tabs.indexOf(tab) : -1;
//    if (index == -1) return;
    for (var i = 1; i != tabs.length; i++) {
        if (index == i) {
            tabPanel.unhideTabStripItem(i);
        } else {
            tabPanel.hideTabStripItem(i);
        }
    }
    tabPanel.doLayout();
};

/**
 * Hides the specified tab.
 * @param {CQ.Ext.Panel} tab The panel
 */
Ejst.x2.hideTab = function(tab) {
    var tabPanel = tab.findParentByType('tabpanel');
    var index = tabPanel.items.indexOf(tab);
    tabPanel.hideTabStripItem(index);
};

/**
 * Shows the tab which ID matches the value of the specified field.
 * @param {CQ.Ext.form.Field} field The field
 */
Ejst.x2.showTab = function(field) {
    Ejst.x2.manageTabs(field.findParentByType('tabpanel'), field.getValue());
};

/**
 * Toggles the field set on the same tab as the check box.
 * @param {CQ.Ext.form.Checkbox} box The check box
 */
Ejst.x2.toggleFieldSet = function(box) {
    var panel = box.findParentByType('panel');
    var fieldSet = panel.findByType('dialogfieldset')[0];
    var show = box.getValue()[0];
    if (show) {
        fieldSet.show();
        
        panel.doLayout();
    } else {
        fieldSet.hide();
        fieldSet.items.each(function(field) {
            try {
                field.setValue();
            } catch (e) {
            }
        });
    }
};

/**
 * Shows some info about a referenced asset.
 * @param {CQ.form.OwnerDraw} ownerdraw The field
 * @param {CQ.Ext.data.Record} rec The record
 * @param {String} path The content path
 */
Ejst.x2.showInfo = function(ownerdraw,rec,path) {
    var html = "";

    if (path) {
        var pagePath = path.substring(0,path.indexOf("/jcr:content") + 12);
        var pageInfo = CQ.HTTP.eval(pagePath + ".1.json");
        html += "<h5>" + CQ.I18n.getMessage("Page info:") + "</h5>";
        html += "<ul>";
        html += "<li>jcr:title=" + pageInfo["jcr:title"] + "</li>";
        html += "<li>cq:lastModifiedBy=" + pageInfo["cq:lastModifiedBy"] + "</li>";
        html += "</ul>";
    }

    html += "<h5>" + CQ.I18n.getMessage("Asset info:") + "</h5>";
    var reference = rec.get(ownerdraw.fileReferenceName);
    if (reference) {
        var metadata = CQ.HTTP.eval(reference + "/jcr:content/metadata.1.json");
        html += "<ul>";
        html += "<li>dc:title=" + metadata["dc:title"] + "</li>";
        html += "<li>dam:Bitsperpixel=" + metadata["dam:Bitsperpixel"] + "</li>";
        html += "<li>dam:Fileformat=" + metadata["dam:Fileformat"] + "</li>";
        html += "</ul>";
    } else {
        html += "<p>" + CQ.I18n.getMessage("No asset referenced") + "</p>";
    }
    ownerdraw.getEl().update(html);
};

//------------------------------------------------------------------------------
// Exercise 3: Custom Widgets
//------------------------------------------------------------------------------
Ejst.x3 = {};

Ejst.x3.provideOptions = function(path, record) {
    // do something with the path or record
    return [{
        text:"1/12",
        value:"1"
    },{
        text:"2/12",
        value:"2"
    },{
        text:"3/12",
        value:"3"
    },{
        text:"4/12",
        value:"4"
    },{
        text:"5/12",
        value:"5"
    },{
        text:"6/12",
        value:"6"
    },{
        text:"7/12",
        value:"7"
    },{
        text:"8/12",
        value:"8"
    },{
        text:"9/12",
        value:"9"
    },{
        text:"10/12",
        value:"10"
    },{
        text:"11/12",
        value:"11"
    },{
        text:"12/12",
        value:"12"
    }];
};

