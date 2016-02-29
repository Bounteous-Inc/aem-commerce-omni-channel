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
package com.adobe.demo.wetelco.mobile.dps.mobileclient.requestprocessing;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

/**
 * Copied from ContentSync.
 * Response object needed to invoke SlingRequestProcessor
 */
public class FakeResponse implements HttpServletResponse {
	private MD5ServletOutputStream outputStream;
	private PrintWriter printWriter;
    private String contentType;
    private String charset;

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusMsg() {
		return statusMsg;
	}

	private int statusCode = -1;
	private String statusMsg = null;

    public FakeResponse(OutputStream out) {
		outputStream = new MD5ServletOutputStream(out);
	}

	public String getMD5() {
		return outputStream.getMD5();
	}

    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }
    
    public PrintWriter getWriter() throws IOException {
    	if(printWriter == null) {
    		//printWriter = new PrintWriter(getOutputStream(), true);
            Writer osWriter = new OutputStreamWriter(getOutputStream(), "UTF-8");
            printWriter = new PrintWriter(osWriter, true);
        }

        return printWriter;
    }

    public void flushBuffer() throws IOException {}

    public void resetBuffer() {}

    public void reset() {}
    
    public boolean isCommitted() {
    	return false;
    }
    
    public void setContentType(String type) {
        contentType = type;
    }

	public void addCookie(Cookie cookie) {
	}

	public void addDateHeader(String name, long date) {
	}

	public void addHeader(String name, String value) {
	}

	public void addIntHeader(String name, int value) {
	}

	public boolean containsHeader(String name) {
		return false;
	}

	public String encodeRedirectURL(String url) {
		return null;
	}

	public String encodeRedirectUrl(String url) {
		return null;
	}

	public String encodeURL(String url) {
		return null;
	}

	public String encodeUrl(String url) {
		return null;
	}

	public void sendError(int sc) throws IOException {
		setStatus(sc);
	}

	public void sendError(int sc, String msg) throws IOException {
		setStatus(sc, msg);
	}

	public void sendRedirect(String location) throws IOException {
	}

	public void setDateHeader(String name, long date) {
	}

	public void setHeader(String name, String value) {
	}

	public void setIntHeader(String name, int value) {
	}

	public void setStatus(int sc) {
		statusCode = sc;
	}

	public void setStatus(int sc, String sm) {
		statusCode = sc;
		statusMsg = sm;
	}

	public int getBufferSize() {
		return 0;
	}

	public String getCharacterEncoding() {
		return charset;
	}

	public String getContentType() {
		return contentType;
	}

	public Locale getLocale() {
		return null;
	}

	public void setBufferSize(int size) {
	}

	public void setCharacterEncoding(String charset) {
        this.charset = charset;
    }

	public void setContentLength(int len) {
	}

	public void setLocale(Locale loc) {
		Locale l = loc;
	}
}
