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

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Copied from ContentSync.
 * Stream needed to invoke SlingRequestProcessor
 */
public class MD5ServletOutputStream extends javax.servlet.ServletOutputStream {
	private OutputStream outputStream;
	private MessageDigest md5;
	
	public MD5ServletOutputStream(OutputStream out) {
		outputStream = out;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException e) {
			// noop
		}
	}

	@Override
	public void write(int b) throws IOException {
		outputStream.write(b);

		if(md5 != null) {
			md5.update(new Integer(b).byteValue());
		}
	}
	
	public String getMD5() {
		return (new BigInteger(1, md5.digest())).toString(16);
	}
}
