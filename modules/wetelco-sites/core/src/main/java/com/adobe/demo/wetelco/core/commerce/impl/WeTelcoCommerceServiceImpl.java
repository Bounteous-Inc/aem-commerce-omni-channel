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
import com.adobe.cq.commerce.api.Product;
import com.adobe.cq.commerce.api.ShippingMethod;
import com.adobe.cq.commerce.common.AbstractJcrCommerceService;
import com.adobe.cq.commerce.common.ServiceContext;
import com.adobe.demo.wetelco.mobile.dps.mobileclient.RequestException;
import com.adobe.demo.wetelco.mobile.dps.services.AEMMoDService;
import com.day.cq.wcm.api.Page;

public class WeTelcoCommerceServiceImpl extends AbstractJcrCommerceService
		implements CommerceService {

	private Resource resource;

	public static final Logger LOGGER = LoggerFactory
			.getLogger(WeTelcoCommerceServiceImpl.class);

	private AEMMoDService aemModService;
	
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
	public void catalogRolloutHook(Page blueprint, Page catalog)
			throws CommerceException {


		// This will be the first method, so make sure to import all
		// collection/articles from
		// AEM MoD
		// TODO - Lab 4 : Uncomment Start
		
		try {
			aemModService.importOnDemandContent(catalog);
		} catch (ServletException e) {
			LOGGER.error("Error during AEM MoD Service Import");
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("Error during AEM MoD Service Import");
			e.printStackTrace();
		} catch (RequestException e) {
			LOGGER.error("Error during AEM MoD Service Import");
			e.printStackTrace();
		} catch (RepositoryException e) {
			LOGGER.error("Error during AEM MoD Service Import");
			e.printStackTrace();
		}
		// TODO - Lab 4 : Uncomment End
	}

	@Override
	public void sectionRolloutHook(Page blueprint, Page section) {

		super.sectionRolloutHook(blueprint, section);
		LOGGER.debug("THIS IS A COLLECTION TO BE CREATED !!!!");

		// TODO - Lab 4 : Uncomment Start
		
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
				LOGGER.error("Error during AEM MoD Service Upload of Collections");
				e.printStackTrace();
			} catch (Exception e) {
				LOGGER.error("Error during AEM MoD Service Upload of Collections");
				e.printStackTrace();
			}
		}
		// TODO - Lab 4 : Uncomment End
	}

	@Override
	public void productRolloutHook(Product productData, Page productPage,
			Product product) throws CommerceException {


		super.productRolloutHook(productData, productPage, product);

		LOGGER.debug("THIS IS A ARTICLE TO BE CREATED !!!!");
		// TODO - Lab 4 : Uncomment Start
		
		try {
			// Then this is a article and we need to create a Collection
			aemModService.createArticle(productPage);
		} catch (RepositoryException e) {
			LOGGER.error("Error during AEM MoD Service Upload of Articles");
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.error("Error during AEM MoD Service Upload of Articles");
			e.printStackTrace();
		}
		// TODO - Lab 4 : Uncomment End
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
