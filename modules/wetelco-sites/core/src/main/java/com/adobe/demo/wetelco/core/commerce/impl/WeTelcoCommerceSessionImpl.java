package com.adobe.demo.wetelco.core.commerce.impl;

import java.math.BigDecimal;
import java.util.Map;

import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.commons.collections.Predicate;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.cq.commerce.api.CommerceConstants;
import com.adobe.cq.commerce.api.CommerceException;
import com.adobe.cq.commerce.api.PlacedOrder;
import com.adobe.cq.commerce.common.AbstractJcrCommerceService;
import com.adobe.cq.commerce.common.AbstractJcrCommerceSession;
import com.day.cq.i18n.I18n;

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
		//
		// A simple shipping pricing architecture with fixed shipping costs.
		//
		String[][] shippingCosts = {
				{ "/etc/commerce/shipping-methods/geometrixx-outdoors/ground",
						"10.00" },
				{
						"/etc/commerce/shipping-methods/geometrixx-outdoors/three-day",
						"20.00" },
				{ "/etc/commerce/shipping-methods/geometrixx-outdoors/two-day",
						"25.00" },
				{
						"/etc/commerce/shipping-methods/geometrixx-outdoors/overnight",
						"40.00" } };

		for (String[] entry : shippingCosts) {
			if (entry[0].equals(method)) {
				return new BigDecimal(entry[1]);
			}
		}
		return BigDecimal.ZERO;
	}

	@Override
	protected String tokenizePaymentInfo(Map<String, String> paymentDetails)
			throws CommerceException {
		//
		// This is only a stub implementation for the Geometrixx-Outdoors demo
		// site, for which there is no
		// real payment processing (or payment info tokenization).
		//
		return "faux-payment-token";
	}

	@Override
	protected void initiateOrderProcessing(String orderPath)
			throws CommerceException {
		//
		// This is only a stub implementation for the Geometrixx-Outdoors demo
		// site, for which there is no
		// real order processing.
		//
	}

	@Override
	protected String getOrderStatus(String orderId) throws CommerceException {
		//
		// Status is kept in the vendor section (/etc/commerce); need to find
		// corresponding order there.
		//
		Session serviceSession = null;
		try {
			serviceSession = commerceService.serviceContext().slingRepository
					.loginService("orders", null);
			//
			// example query:
			// /jcr:root/etc/commerce/orders//element(*)[@orderId='foo')]
			//
			StringBuilder buffer = new StringBuilder();
			buffer.append(
					"/jcr:root/etc/commerce/orders//element(*)[@orderId = '")
					.append(Text.escapeIllegalXpathSearchChars(orderId)
							.replaceAll("'", "''")).append("']");

			final Query query = serviceSession.getWorkspace().getQueryManager()
					.createQuery(buffer.toString(), Query.XPATH);
			NodeIterator nodeIterator = query.execute().getNodes();
			if (nodeIterator.hasNext()) {
				return nodeIterator.nextNode().getProperty("orderStatus")
						.getString();
			}
		} catch (Exception e) {
			// fail-safe when the query above contains errors
			log.error("Error while fetching order status for orderId '"
					+ orderId + "'", e);
		} finally {
			if (serviceSession != null) {
				serviceSession.logout();
			}
		}
		final I18n i18n = new I18n(request);
		return i18n.get("unknown", "order status");
	}

	@Override
	protected Predicate getPredicate(String predicateName) {
		//
		// This stub implementation supports only the openOrders predicate.
		//
		if (predicateName != null
				&& predicateName
						.equals(CommerceConstants.OPEN_ORDERS_PREDICATE)) {
			return new Predicate() {
				public boolean evaluate(Object object) {
					try {
						PlacedOrder order = (PlacedOrder) object;
						String status = (String) order.getOrder().get(
								"orderStatus");
						return (status != null && !status.equals("Completed") && !status
								.equals("Cancelled"));
					} catch (CommerceException e) {
						return false;
					}
				}
			};
		}
		return null;
	}
}
