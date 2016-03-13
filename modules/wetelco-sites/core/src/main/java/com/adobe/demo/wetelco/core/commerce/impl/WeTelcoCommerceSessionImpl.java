package com.adobe.demo.wetelco.core.commerce.impl;

import java.math.BigDecimal;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.cq.commerce.api.CommerceException;
import com.adobe.cq.commerce.common.AbstractJcrCommerceService;
import com.adobe.cq.commerce.common.AbstractJcrCommerceSession;

public class WeTelcoCommerceSessionImpl extends AbstractJcrCommerceSession {

	protected SlingHttpServletRequest request;
	protected SlingHttpServletResponse response;
	protected Resource resource;
	protected ResourceResolver resolver;
	protected AbstractJcrCommerceService commerceService;

	public WeTelcoCommerceSessionImpl(
			AbstractJcrCommerceService commerceService,
			SlingHttpServletRequest request, SlingHttpServletResponse response,
			Resource resource) throws CommerceException {
		super(commerceService, request, response, resource);

		this.request = request;
		this.response = response;
		this.resource = resource;
		this.resolver = resource.getResourceResolver();
		this.commerceService = commerceService;

	}

	@Override
	protected BigDecimal getShipping(String method) {
		return BigDecimal.ZERO;
	}

}
