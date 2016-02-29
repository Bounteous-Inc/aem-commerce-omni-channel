/*************************************************************************
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
 **************************************************************************/
package com.adobe.demo.wetelco.mobile.dps.mobileclient;

/**
 * Exception resulting from AEM Mobile web requests
 */
public class RequestException extends Exception {

    private String request = null;
    private int code = -1;

    /**
     * Constructs a new exception.
     *
     * @param message the detailed message
     * @param cause   the cause of this exception
     */
    public RequestException(String request, int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Constructs a new exception.
     *
     * @param message the detailed message
     */
    public RequestException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getRequest() {
        return request;
    }

    public String getDetailedMessage(){
        return "["+getCode()+"] " + getMessage() + " ["+getRequest()+"]";
    }
}
