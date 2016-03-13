package com.adobe.demo.wetelco.core.commerce.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.commerce.api.CommerceConstants;
import com.adobe.cq.commerce.api.CommerceException;
import com.adobe.cq.commerce.api.CommerceService;
import com.adobe.cq.commerce.api.CommerceSession;
import com.adobe.cq.commerce.api.Product;
import com.adobe.cq.commerce.api.ShippingMethod;
import com.adobe.cq.commerce.common.AbstractJcrCommerceService;
import com.adobe.cq.commerce.common.ServiceContext;
import com.day.cq.wcm.api.Page;

public class WeTelcoCommerceServiceImpl extends AbstractJcrCommerceService
		implements CommerceService {

	private Resource resource;

	public static final Logger LOGGER = LoggerFactory
			.getLogger(WeTelcoCommerceServiceImpl.class);

	public WeTelcoCommerceServiceImpl(ServiceContext serviceContext,
			Resource resource) {
		super(serviceContext, resource);
		this.resource = resource;
	}

	@Override
	public CommerceSession login(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws CommerceException {
		// TODO LAB2 : START

		return null;

		// TODO LAB2 : END
	}

	@Override
	public boolean isAvailable(String serviceType) {
		if (CommerceConstants.SERVICE_COMMERCE.equals(serviceType)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Product getProduct(final String path) throws CommerceException {
		Resource resource = resolver.getResource(path);
		if (resource != null
				&& WeTelcoProductImpl.isAProductOrVariant(resource)) {
			return new WeTelcoProductImpl(resource);
		}
		return null;
	}

	@Override
	public void catalogRolloutHook(Page blueprint, Page catalog)
			throws CommerceException {

		// TODO: Lab 4

	}

	@Override
	public void sectionRolloutHook(Page blueprint, Page section) {
		super.sectionRolloutHook(blueprint, section);

		// TODO: Lab 4

	}

	@Override
	public void productRolloutHook(Product productData, Page productPage,
			Product product) throws CommerceException {

		// TODO: Lab 4

	}

	@Override
	public List<String> getCountries() throws CommerceException {
		List<String> countries = new ArrayList<String>();

		// A true implementation would likely need to check with its payment
		// processing and/or
		// fulfillment services to determine what countries to accept. This
		// implementation
		// simply accepts them all.
		countries.add("*");

		return countries;
	}

	@Override
	public List<String> getCreditCardTypes() throws CommerceException {
		List<String> ccTypes = new ArrayList<String>();

		// A true implementation would likely need to check with its payment
		// processing
		// service to determine what credit cards to accept. This implementation
		// simply
		// accepts them all.
		ccTypes.add("*");

		return ccTypes;
	}

	@Override
	public List<String> getOrderPredicates() throws CommerceException {
		List<String> predicates = new ArrayList<String>();
		predicates.add(CommerceConstants.OPEN_ORDERS_PREDICATE);
		return predicates;
	}

	@Override
	public List<ShippingMethod> getAvailableShippingMethods()
			throws CommerceException {
		return enumerateMethods("/etc/commerce/shipping-methods/wetelco",
				ShippingMethod.class);
	}

}
