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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Copied from ContentSync.
 * Request object needed to invoke SlingRequestProcessor
 */
public class FakeRequest implements HttpServletRequest {
	private final String method;
	private final String path;
	private final Map<String, Object> attributes;
	private final Map<String, String[]> parameters;
    private final HttpSession session;

    private static final class FakeHttpSession implements HttpSession {
        public long getCreationTime() {
            throw new UnsupportedOperationException();
        }

        public String getId() {
            throw new UnsupportedOperationException();
        }

        public long getLastAccessedTime() {
            throw new UnsupportedOperationException();
        }

        public ServletContext getServletContext() {
            throw new UnsupportedOperationException();
        }

        public void setMaxInactiveInterval(int i) {
            throw new UnsupportedOperationException();
        }

        public int getMaxInactiveInterval() {
            throw new UnsupportedOperationException();
        }

        public HttpSessionContext getSessionContext() {
            throw new UnsupportedOperationException();
        }

        public Object getAttribute(String s) {
            throw new UnsupportedOperationException();
        }

        public Object getValue(String s) {
            throw new UnsupportedOperationException();
        }

        public Enumeration getAttributeNames() {
            throw new UnsupportedOperationException();
        }

        public String[] getValueNames() {
            throw new UnsupportedOperationException();
        }

        public void setAttribute(String s, Object o) {
            throw new UnsupportedOperationException();
        }

        public void putValue(String s, Object o) {
            throw new UnsupportedOperationException();
        }

        public void removeAttribute(String s) {
            throw new UnsupportedOperationException();
        }

        public void removeValue(String s) {
            throw new UnsupportedOperationException();
        }

        public void invalidate() {
            throw new UnsupportedOperationException();
        }

        public boolean isNew() {
            throw new UnsupportedOperationException();
        }
    }

    public FakeRequest(String method, String path) {
    	this.method = method;
    	this.path = path;
    	this.attributes = new HashMap<String, Object>();
    	this.parameters = new HashMap<String, String[]>();
        this.session = new FakeHttpSession();
	}

    public FakeRequest(String method, String path, Map<String, Object> params) {
    	this.method = method;
    	this.path = path;
    	this.attributes = new HashMap<String, Object>();
    	this.parameters = new HashMap<String, String[]>();
        this.session = new FakeHttpSession();
    	
    	for(String key : params.keySet()) {
    		Object value = params.get(key);

    		// internally, Sling seems to expect all parameter values to be String[]
    		if(params.get(key) instanceof String[]) {
    			this.parameters.put(key, (String[])value);
    		} else {
    			this.parameters.put(key, new String[]{ value.toString() });
    		}
    	}
	}

	public String getAuthType() {
		return null;
	}

	public String getContextPath() {
		return "";
	}

	public Cookie[] getCookies() {
		return new Cookie[0];
	}

	public long getDateHeader(String name) {
		return -1;
	}

	public String getHeader(String name) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Enumeration getHeaderNames() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Enumeration getHeaders(String name) {
		return null;
	}

	public int getIntHeader(String name) {
		return -1;
	}

	public String getMethod() {
		return method;
	}

	public String getPathInfo() {
		return null;
	}

	public String getPathTranslated() {
		return null;
	}

	public String getQueryString() {
		return null;
	}

	public String getRemoteUser() {
		return null;
	}

	public String getRequestURI() {
		return path;
	}

	public StringBuffer getRequestURL() {
		return new StringBuffer("http://localhost:4502" + path);
	}

	public String getRequestedSessionId() {
		return null;
	}

	public String getServletPath() {
		return path;
	}

	public HttpSession getSession() {
		return session;
	}

	public HttpSession getSession(boolean create) {
		return session;
	}

	public Principal getUserPrincipal() {
		return null;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	public boolean isRequestedSessionIdValid() {
		return false;
	}

	public boolean isUserInRole(String role) {
		return false;
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	public String getCharacterEncoding() {
		return "utf-8";
	}

	public int getContentLength() {
		return 0;
	}

	public String getContentType() {
		return null;
	}

	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	public String getLocalAddr() {
		return null;
	}

	public String getLocalName() {
		return null;
	}

	public int getLocalPort() {
		return 0;
	}

	public Locale getLocale() {
		return new Locale("en");
	}

	@SuppressWarnings("unchecked")
	public Enumeration getLocales() {
		return null;
	}

	public String getParameter(String name) {
		try {
			final Object value = parameters.get(name);
			
			if(value instanceof String[]) {
				return ((String[])value)[0];
			}
			
			return (String)value;
		} catch(ClassCastException e) {
			return null;
		}
	}

	public Map<String, String[]> getParameterMap() {
		return parameters;
	}

	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	public String[] getParameterValues(String name) {
		throw new UnsupportedOperationException();
	}

	public String getProtocol() {
		return "HTTP/1.1";
	}

	public BufferedReader getReader() throws IOException {
		return null;
	}

	public String getRealPath(String path) {
		return null;
	}

	public String getRemoteAddr() {
		return null;
	}

	public String getRemoteHost() {
		return null;
	}

	public int getRemotePort() {
		return 0;
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	public String getScheme() {
		return "http";
	}

	public String getServerName() {
		return null;
	}

	public int getServerPort() {
		return 0;
	}

	public boolean isSecure() {
		return false;
	}

	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
	}
}
