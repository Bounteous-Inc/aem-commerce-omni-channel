package com.adobe.demo.wetelco.core.commerce.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.commerce.api.CommerceConstants;
import com.adobe.cq.commerce.api.CommerceException;
import com.adobe.cq.commerce.api.CommerceService;
import com.adobe.cq.commerce.api.CommerceSession;
import com.adobe.cq.commerce.api.PaymentMethod;
import com.adobe.cq.commerce.api.Product;
import com.adobe.cq.commerce.api.ShippingMethod;
import com.adobe.cq.commerce.api.promotion.Voucher;
import com.adobe.cq.commerce.common.AbstractJcrCommerceService;
import com.adobe.cq.commerce.common.ServiceContext;
import com.adobe.cq.commerce.common.promotion.AbstractJcrVoucher;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.RequestException;
import com.adobe.demo.wetelco.mobile.dps.services.AEMMoDService;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;

public class WeTelcoCommerceServiceImpl extends AbstractJcrCommerceService
		implements CommerceService {

	private Resource resource;
	private AEMMoDService aemModService;

	public static final Logger LOGGER = LoggerFactory
			.getLogger(WeTelcoCommerceServiceImpl.class);

	public WeTelcoCommerceServiceImpl(ServiceContext serviceContext,
			Resource resource, AEMMoDService aemModService) {
		super(serviceContext, resource);
		this.resource = resource;
		this.aemModService = aemModService;
	}

	@Override
	public CommerceSession login(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws CommerceException {
		return new WeTelcoCommerceSessionImpl(this, request, response, resource);
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
	public Voucher getVoucher(final String path) throws CommerceException {
		Resource resource = resolver.getResource(path);
		if (resource != null) {
			// JCR-based vouchers are cq:Pages
			Resource contentResource = resource
					.getChild(JcrConstants.JCR_CONTENT);
			if (contentResource != null
					&& contentResource
							.isResourceType(AbstractJcrVoucher.VOUCHER_RESOURCE_TYPE)) {
				return new AbstractJcrVoucher(resource);
			}
		}
		return null;
	}

	@Override
	public void catalogRolloutHook(Page blueprint, Page catalog)
			throws CommerceException {

		// This will be the first method, so make sure to import all
		// collection/articles from
		// AEM MoD
		try {
			aemModService.importOnDemandContent(catalog);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void sectionRolloutHook(Page blueprint, Page section) {
		super.sectionRolloutHook(blueprint, section);

		// 1. Check if the blueprint is having a property for DPS Rollout
		ValueMap listOfProperties = section.getProperties();
		Object dpsResourceType = listOfProperties.get("dps-isRolloutout");

		// 2. If the rollout is for DPS Rollout, then make sure that the secion
		// goes in as category
		if (dpsResourceType != null) {
			// Then this is a article and we need to create a Collection
			try {

				LOGGER.debug("START - COLLECTION TO BE CREATED !!!!");
				aemModService.createCollection(section);
				LOGGER.debug("END - COLLECTION TO BE CREATED !!!!");

			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void productRolloutHook(Product productData, Page productPage,
			Product product) throws CommerceException {

		super.productRolloutHook(productData, productPage, product);

		LOGGER.debug("THIS IS A ARTICLE TO BE CREATED !!!!");

		try {
			// Then this is a article and we need to create a Collection
			aemModService.createArticle(productPage);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<ShippingMethod> getAvailableShippingMethods()
			throws CommerceException {
		return enumerateMethods("/etc/commerce/shipping-methods/wetelco",
				ShippingMethod.class);
	}

	@Override
	public List<PaymentMethod> getAvailablePaymentMethods()
			throws CommerceException {
		return enumerateMethods("/etc/commerce/payment-methods/wetelco",
				PaymentMethod.class);
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

}
