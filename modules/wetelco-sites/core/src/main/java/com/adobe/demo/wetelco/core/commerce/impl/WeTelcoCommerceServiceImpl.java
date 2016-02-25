package com.adobe.demo.wetelco.core.commerce.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
import com.adobe.demo.wetelco.mobile.dps.operations.CreateArticleService;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;

public class WeTelcoCommerceServiceImpl extends AbstractJcrCommerceService
		implements CommerceService {

	private Resource resource;
	private CreateArticleService createArticleService;
	public static final Logger LOGGER = LoggerFactory
			.getLogger(WeTelcoCommerceServiceImpl.class);

	private static final String WETELCO_AEM_MOBILE_PROJECT = "/content/mobileapps/wetelco-aem-mobile-app";

	public WeTelcoCommerceServiceImpl(ServiceContext serviceContext,
			Resource resource, CreateArticleService createArticleService) {
		super(serviceContext, resource);
		this.resource = resource;
		this.createArticleService = createArticleService;
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
		Resource content = blueprint.getContentResource();
		if (content == null)
			return;

		Resource templates = content.getChild("templates");
		if (templates == null)
			return;

		String catalogTemplatePath = templates.adaptTo(ValueMap.class).get(
				"catalog", "");
		if (catalogTemplatePath.trim().length() == 0)
			return;

		Resource catalogTemplate = templates.getResourceResolver().getResource(
				catalogTemplatePath);
		if (catalogTemplate == null)
			return;

		Resource toolbar = catalogTemplate.getChild("toolbar");
		if (toolbar == null)
			return;

		// copy the toolbar node from the catalog template
		try {
			JcrUtil.copy(toolbar.adaptTo(Node.class),
					catalog.adaptTo(Node.class), "toolbar");
		} catch (Exception x) {
			throw new CommerceException(x.getMessage(), x);
		}

		// set cq:offlinePage fot the catalog, in the template this is set
		// relative to the root of the catalog
		String offlinePage = catalogTemplate.getChild("jcr:content")
				.adaptTo(ValueMap.class).get("cq:offlinePage", "");
		Node catalogContent = catalog.getContentResource().adaptTo(Node.class);
		try {
			catalogContent.setProperty("cq:offlinePage", catalog.getPath()
					+ "/" + offlinePage);
		} catch (Exception x) {
			throw new CommerceException(x.getMessage(), x);
		}

		// set cq:signinPage fot the catalog, in the template this is set
		// relative to the root of the catalog
		String signinPage = catalogTemplate.getChild("jcr:content")
				.adaptTo(ValueMap.class).get("cq:signinPage", "");
		try {
			catalogContent.setProperty("cq:signinPage", catalog.getPath() + "/"
					+ signinPage);
		} catch (Exception x) {
			throw new CommerceException(x.getMessage(), x);
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
			if (createArticleService != null) {
				// createArticleService.createCollection(session,
				// collectionNode);
				LOGGER.debug("THIS IS A COLLECTION TO BE CREATED !!!!");

				try {
					
					// 2. If the node location is not available, don't do
					// anything.
					// Then this is a article and we need to create a Collection
					if (createArticleService != null) {
						// createArticleService.createCollection(session,
						// collectionNode);
						LOGGER.debug("THIS IS A COLLECTION TO BE CREATED !!!!");

						createArticleService.createCollection(section);
					}

					// 3. Check if there are collections folder available. If
					// not available
					// create the collection folder under there.

					// 4. Copy across the currently created Section pages under
					// the collections
					// folder

				} catch (RepositoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	@Override
	public void productRolloutHook(Product productData, Page productPage,
			Product product) throws CommerceException {

		super.productRolloutHook(productData, productPage, product);

		// 1. First get the node location for WeTelco AEM Mobile
		// 1. Check if the blueprint is having a property for DPS Rollout
		ValueMap listOfProperties = productPage.getProperties();
		Object dpsResourceType = listOfProperties.get("dps-isRolloutout");

		// 2. If the rollout is for DPS Rollout, then make sure that the secion
		// goes in as category
		if (dpsResourceType != null) {
			// Then this is a article and we need to create a Collection
			if (createArticleService != null) {
				// createArticleService.createCollection(session,
				// collectionNode);
				LOGGER.debug("THIS IS A ARTICLE TO BE CREATED !!!!");

				try {
					// 1. First get the node location for WeTelco AEM Mobile
					Node articleNode = productPage.adaptTo(Node.class);
					Session sessionToCreateArticle = articleNode.getSession();

					// 2. If the node location is not available, don't do
					// anything.
					// Then this is a article and we need to create a Collection
					createArticleService.createArticle(productPage);

				} catch (RepositoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * @Override public void productRolloutHook(Product productData, Page
	 * productPage, Product product) throws CommerceException {
	 * 
	 * 
	 * // 1. Check if the blueprint is having a property for DPS Rollout
	 * ValueMap listOfProperties = productPage.getProperties(); Object
	 * dpsResourceType = listOfProperties.get("dps-resourceType");
	 * 
	 * // 2. If the rollout is for DPS Rollout, then make sure that the secion
	 * // goes in as category if (dpsResourceType != null) { // Then this is a
	 * article and we need to create a Collection if (createArticleService !=
	 * null) { // createArticleService.createCollection(session, //
	 * collectionNode); LOGGER.debug("THIS IS A COLLECTION TO BE CREATED !!!!");
	 * } }
	 * 
	 * 
	 * try { boolean changed = false;
	 * 
	 * // // The out-of-the-box commerce components (such as //
	 * commerce/components/product) support // two variant axes: "size", plus
	 * one (optional) user-defined axis. // The user-defined axis, if required,
	 * is specified using the // "variationAxis" and // "variationTitle"
	 * properties. // // In the geometrixx sample product set, the optional axis
	 * is always // "color". // Node productNode = product.adaptTo(Node.class);
	 * if (productData.axisIsVariant("color")) { if
	 * (!productNode.hasProperty("variationAxis")) {
	 * productNode.setProperty("variationAxis", "color");
	 * productNode.setProperty("variationTitle", "Color"); changed = true; } }
	 * else { if (productNode.hasProperty("variationAxis") &&
	 * productNode.getProperty("variationAxis").getString() .equals("color")) {
	 * productNode.setProperty("variationAxis", "");
	 * productNode.setProperty("variationTitle", ""); changed = true; } }
	 * 
	 * // // Copy geometrixx-outdoors-namespaced tags from the product to the //
	 * product page. // if (CommerceHelper.copyTags(productData,
	 * productPage.getContentResource(), new Predicate() { public boolean
	 * evaluate(Object o) { return ((Tag) o).getNamespace().getName()
	 * .equals("weltelco"); } })) { changed = true; }
	 * 
	 * // // Give product pages a product-specific thumbnail so they don't //
	 * have to fall back to // the (generic) page_product template's thumbnail.
	 * This greatly // improves the usability // of the pages content finder
	 * tab. // if (!ResourceUtil.isA(productPage.getContentResource(),
	 * CommerceConstants.RT_PRODUCT_PAGE_PROXY)) { String productImageRef = "";
	 * Resource productImage = productData.getImage(); if (productImage != null)
	 * { productImageRef = ResourceUtil.getValueMap(productImage)
	 * .get("fileReference", ""); } Node contentNode =
	 * productPage.getContentResource().adaptTo( Node.class); Node pageImageNode
	 * = JcrUtils.getOrAddNode(contentNode, "image", "nt:unstructured");
	 * pageImageNode.setProperty("fileReference", productImageRef); }
	 * 
	 * if (changed) { productPage.getPageManager().touch(
	 * productPage.adaptTo(Node.class), true, Calendar.getInstance(), false); }
	 * } catch (Exception e) { throw new
	 * CommerceException("Product rollout hook failed: ", e); } }
	 */
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
