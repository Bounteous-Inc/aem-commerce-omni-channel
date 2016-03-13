package com.adobe.demo.wetelco.core.commerce;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;

import com.adobe.cq.commerce.api.CommerceService;
import com.adobe.cq.commerce.api.CommerceServiceFactory;
import com.adobe.cq.commerce.common.AbstractJcrCommerceServiceFactory;
import com.adobe.demo.wetelco.core.commerce.impl.WeTelcoCommerceServiceImpl;

/**
 * Implementation for the {@link CommerceServiceFactory} interface for WeTelco
 * Product Catalog.
 */
@Component(metatype = true, label = "WeTelco Product Catalog Commerce factory")
@Service
@Properties(value = {
		@Property(name = "service.description", value = "Factory for WeTelco Product Catalog commerce service"),
		@Property(name = "commerceProvider", value = "wetelco") })
public class WeTelcoCommerceServiceFactory extends
		AbstractJcrCommerceServiceFactory implements CommerceServiceFactory {

	public CommerceService getCommerceService(Resource res) {
		return new WeTelcoCommerceServiceImpl(getServiceContext(), res);
	}

}
