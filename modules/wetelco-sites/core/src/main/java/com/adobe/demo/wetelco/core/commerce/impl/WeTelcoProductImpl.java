package com.adobe.demo.wetelco.core.commerce.impl;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.commerce.common.AbstractJcrProduct;

public class WeTelcoProductImpl extends AbstractJcrProduct {
	public static final Logger LOGGER = LoggerFactory
			.getLogger(WeTelcoProductImpl.class);
	public static final String PRODUCT_IDENTIFIER = "identifier";

	public WeTelcoProductImpl(Resource resource) {
		super(resource);
	}

	public String getSKU() {
		String productIdentifier = super.getProperty(PRODUCT_IDENTIFIER,
				String.class);
		return productIdentifier;
	}
}
