<%@page session="false"%><%--
  ADOBE CONFIDENTIAL
  __________________

   Copyright 2012 Adobe Systems Incorporated
   All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.
--%>
<%@ include file="/libs/foundation/global.jsp"%>
<%@ page contentType="text/html; charset=utf-8"
	import="
		java.util.ArrayList,
		java.util.Iterator,
		java.util.List,
	    java.util.ResourceBundle,
		java.util.Locale,
		org.apache.commons.lang.StringUtils,
		com.adobe.cq.commerce.api.CommerceService,
		com.adobe.cq.commerce.api.CommerceSession,
		com.adobe.cq.commerce.api.Product,
		com.adobe.cq.commerce.common.AxisFilter,
		com.adobe.cq.commerce.common.CommerceHelper,
		com.adobe.cq.commerce.common.EnumerateAxisFilter,
		com.day.cq.wcm.api.components.DropTarget,
		com.day.cq.i18n.I18n"%>
<%
%>
<%
    final Locale pageLocale = currentPage.getLanguage(false);
    final ResourceBundle bundle = slingRequest.getResourceBundle(pageLocale);
    final I18n i18n = new I18n(bundle);

    final String language = pageLocale.getLanguage();

    CommerceService commerceService = resource.adaptTo(CommerceService.class);
    CommerceSession session = commerceService.login(slingRequest, slingResponse);

    %><cq:include script="init.jsp" />
<%

    String addToCartUrl = (String) request.getAttribute("cq.commerce.addToCartUrl");
    String addToSmartListUrl = (String) request.getAttribute("cq.commerce.addToSmartListUrl");
    String redirect = (String) request.getAttribute("cq.commerce.redirect");
    String smartListRedirect = (String) request.getAttribute("cq.commerce.smartListRedirect");
    String errorRedirect = (String) request.getAttribute("cq.commerce.errorRedirect");
    Product baseProduct = (Product) request.getAttribute("cq.commerce.product");

    Resource baseProductImage = baseProduct.getImage();

    // This product component renders products with up to two variant axes:
    //   1) the product can have a variant "size" axis
    //   2) the product can have a second variant axis defined by properties:
    //      a) variationAxis: the axis name
    //      b) variationTitle: the text to describe the axis in the renderer HTML
    //      c) variationLead: the value on the variation axis to present by default
    //
    // All variations are rendered to the HTML, and client-side javascript hides/shows them as
    // necessary.  A fragmentId (hash parameter) to the page selects the particular variation
    // to show; in its absence the default variation is shown.
    //
    // Variations along the named axis are complete (ie: any and all of the product properties
    // can vary with each instance).  Variations along the size axis can only vary size and
    // price.

    List<Product> variations = new ArrayList<Product>();

    String variationAxis = baseProduct.getProperty("variationAxis", String.class);
    String variationTitle = i18n.getVar(baseProduct.getProperty("variationTitle", language, String.class));
    String variationLead = baseProduct.getProperty("variationLead", String.class);

    if (StringUtils.isNotEmpty(variationAxis)) {
        Iterator<Product> unorderedVariations = baseProduct.getVariants(new EnumerateAxisFilter(variationAxis));
        while (unorderedVariations.hasNext()) {
            Product variation = unorderedVariations.next();
            if (StringUtils.isNotEmpty(variationLead) && variation.getProperty(variationAxis, String.class).equals(variationLead)) {
                variations.add(0, variation);   // push the variationLead to the front
            } else {
                variations.add(variation);
            }
        }
        if (StringUtils.isEmpty(variationTitle)) {
            variationTitle = i18n.get("Product Variants");
        }
    }

    // If no variations specified, or none existed, just go with the base product:
    if (variations.isEmpty()) {
        variations.add(baseProduct);
    }

%>
<script type="text/javascript">

	$CQ(document).on("sitecatalystAfterCollect", function(event) {
        if (CQ_Analytics.Sitecatalyst) {
            CQ_Analytics.record({
                    "event": ["prodView"],
                    "values": {
                    	"product": [{
                    		"category": "",
                    		"sku": "<%= xssAPI.encodeForJSString(baseProduct.getSKU()) %>"
                    	}]
                    },
                    "componentPath": "<%= xssAPI.encodeForJSString(resource.getResourceType()) %>"
            });
        }
	});

    function validateProductQuantity(fieldId) {
        var quantity = document.getElementById(fieldId).value;
        if (quantity.length == 0 || (quantity.match(/^\d+$/) && Number(quantity) > 0)) {
            return true;
        } else {
            alert('<%= i18n.get("Quantity must be a positive number.") %>');
            document.getElementById(fieldId).value = "";
            return false;
        }
    }

    function trackCartAdd(form) {
        if (window.ContextHub && ContextHub.getStore("abandonedproducts")) {
            ContextHub.getStore("abandonedproducts").record(
                    '<%= xssAPI.encodeForJSString(baseProduct.getPagePath()) %>',
                    '<%= xssAPI.encodeForJSString(baseProduct.getTitle(language)) %>',
                    '<%= xssAPI.encodeForJSString(baseProductImage != null ? resourceResolver.map(baseProductImage.getPath()) : "") %>',
                    '<%= xssAPI.encodeForJSString(session.getProductPrice(baseProduct))%>');
        }
        if (CQ_Analytics.Sitecatalyst) {
            var productQuantity = Number($("input[name='product-quantity']", form).val() || '1');
            var productPrice    = Number($("input[name='product-size']:checked", form).data('price').replace(/[^0-9\\.]/g, ''));
            var productChildSku =        $("input[name='product-size']:checked", form).data('sku');
            CQ_Analytics.record({
                    "event": ["cartAdd"<%= (session.getCartEntryCount() == 0) ? ", 'cartOpen'" : "" %>],
                    "values": {
                    	"product": [{
                    		"category": "",
                    		"sku": "<%= xssAPI.encodeForJSString(baseProduct.getSKU()) %>",
                            "price": productPrice * productQuantity,
                            "quantity": productQuantity,
                    		"evars": {
                            	"childSku": CQ.shared.Util.htmlEncode(productChildSku)
                    		}
                    	}]
                    },
                    "componentPath": "<%= xssAPI.encodeForJSString(resource.getResourceType()) %>"
            });
        }
        return true;
    }

    function trackProductViewed() {
        if (window.ContextHub && ContextHub.getStore("recentlyviewed")) {
            ContextHub.getStore("recentlyviewed").record(
                    '<%= xssAPI.encodeForJSString(baseProduct.getPagePath()) %>',
                    '<%= xssAPI.encodeForJSString(baseProduct.getTitle(language)) %>',
                    '<%= xssAPI.encodeForJSString(baseProductImage != null ? resourceResolver.map(baseProductImage.getPath()) : "") %>',
                    '<%= xssAPI.encodeForJSString(session.getProductPrice(baseProduct))%>');
        }
        if (CQ_Analytics && CQ_Analytics.ViewedProducts) {
            CQ_Analytics.ViewedProducts.record(
                    '<%= xssAPI.encodeForJSString(baseProduct.getPagePath()) %>',
                    '<%= xssAPI.encodeForJSString(baseProduct.getTitle(language)) %>',
                    '<%= xssAPI.encodeForJSString(baseProductImage != null ? resourceResolver.map(baseProductImage.getPath()) : "") %>',
                    '<%= xssAPI.encodeForJSString(session.getProductPrice(baseProduct))%>');
        }	
    }

    function selectVariationAndSize() {
        if (window.location.hash) {
            var hashSku = window.location.hash.slice(1);
            var hashSize = $(".product-size input[data-sku='"+hashSku+"']");
            if (hashSize.length > 0) {
                $("article").addClass("isHidden");
                hashSize.parents("article").removeClass("isHidden");
                hashSize.click();
                return;
            } else {
                var hashVariation = $("li[data-sku='"+hashSku+"']");
                if (hashVariation.length > 0) {
                    $("article").addClass("isHidden");
                    hashVariation.parents("article").removeClass("isHidden");
                    hashVariation.click();
                    return;
                }
            }

            if (window.location.hash.length > 0) {
                window.location.hash = "";
            }
        }
        var defaultSize = $("article:visible").find(".product-size input:first");
        defaultSize.click();
    }

    function addToSmartListActions() {
        var doc = $CQ(document),
                addToSmartListDialog = $CQ('<div/>', {
                    'class': 'smartlist-dialog-add-to-smartlist',
                    'title': 'Add to Smart List'
                }).appendTo('body');

        addToSmartListDialog.dialog({
            autoOpen: false,
            modal: true,
            height: 220,
            width: 320,
            zIndex: 90000
        });
        doc.on("click", ".product-submit .product-wishlist .default", function (e) {
            e.preventDefault();
            $CQ(this).closest("form").submit();
        });
        doc.on("click", ".product-submit .product-wishlist .more", function (e) {
            e.preventDefault();
            var elem = $CQ(this);
            params = {
                'productPath': elem.closest("form").find("input[name='product-path']").val()
            };
            $CQ.get(elem.attr('href'), params, function (html) {
                addToSmartListDialog.html(html).dialog("open");
            });
        });
    }

    $CQ(document).ready(function() {
        selectVariationAndSize();
        trackProductViewed();
        addToSmartListActions();
    });
</script>
<%

    String articleVisibility = ""; // possible values: empty: visible, "isHidden": hidden
    for (Product product : variations) {
        request.setAttribute("cq.commerce.product", product);
%>
<article
	class="product <%= DropTarget.CSS_CLASS_PREFIX %>product-data-reference <%= articleVisibility%>"
	itemscope itemtype="http://schema.org/Product"

	data-sku="<%= product.getSKU() %>">

	<div class="product-viewer" itemprop="image">

		<% Resource assetRes = product.getAsset();
        if (assetRes != null) { %>
		<sling:include resource="<%= assetRes%>" />
		<% } %>
	</div>

		<div class="product-details">
			<cq:include script="product_header.jsp" />
			<cq:include script="product_specs.jsp" />
		</div>

		<% if (variations.size() > 1) { %>
		<section class="product-chooser">
			<h3><%= xssAPI.filterHTML(variationTitle) %></h3>
			<ul>
				<% for (Product variant : variations) { %>
				<li
					title="<%= xssAPI.encodeForHTMLAttr(variant.getTitle(language)) %>"
					data-sku="<%= variant.getSKU() %>">
					<% String thumbnail = variant.getThumbnailUrl();
                        if (StringUtils.isNotEmpty(thumbnail)) { %> <img
					src="<%= xssAPI.getValidHref(thumbnail) %>"
					alt="<%= xssAPI.encodeForHTMLAttr(variant.getTitle(language)) %>" />
					<% } else { %> <span><%= xssAPI.encodeForHTML(variant.getTitle(language)) %></span>
					<% } %>
				</li>
				<% } %>
			</ul>
		</section>
		<% } %>
		<% final String productQuantityId = xssAPI.encodeForHTMLAttr("product-quantity-" + System.currentTimeMillis()); %>

		<form class="product-form" method="POST"
			action="<%= xssAPI.getValidHref(addToCartUrl) %>"
			onsubmit="return validateProductQuantity('<%= productQuantityId %>') && trackCartAdd(this)">
			<div class="product-size-quantity">
				<% if (product.axisIsVariant("size")) {
                   String initialSize = product.getProperty("size", language, String.class); %>
				<section class="product-size">
					<h3><%= xssAPI.filterHTML(i18n.get("Size")) %></h3>
					<ul>
						<% AxisFilter filter = null;
                            if (StringUtils.isNotEmpty(variationAxis)) {
                                filter = new AxisFilter(variationAxis, product.getProperty(variationAxis, String.class));
                            }
                            List<Product> sizes = new ArrayList<Product>();
                            for (Iterator<Product> unorderedSizes = product.getVariants(filter); unorderedSizes.hasNext(); ) {
                                sizes.add(unorderedSizes.next());
                            }
                            java.util.Collections.sort(sizes, CommerceHelper.getProductSizeComparator());
    
                            for (Product p : sizes) {
                                String title = p.getTitle(language);
                                String description = p.getDescription(language);
                                String size = p.getProperty("size", language, String.class);
                                final String sizeId = xssAPI.encodeForHTMLAttr("size-" + size + "-" + System.currentTimeMillis());
                                String checked = (size != null && size.equals(initialSize)) ? "checked='checked'" : ""; %>
						<li><span><label for="<%= sizeId %>"><%= xssAPI.encodeForHTML(size) %></label></span>
							<input type="radio" name="product-size" id="<%= sizeId %>"
							value="<%= xssAPI.encodeForHTMLAttr(size) %>" <%= checked %>
							data-price="<%= session.getProductPrice(p) %>"
							data-sku="<%= xssAPI.encodeForHTMLAttr(p.getSKU()) %>"
							data-path="<%= xssAPI.encodeForHTMLAttr(p.getPath()) %>"
							data-title="<%= title != null ? xssAPI.encodeForHTMLAttr(title) : "" %>"
							data-description="<%= description != null ? xssAPI.encodeForHTMLAttr(description) : "" %>" /></li>
						<% } %>
					</ul>
				</section>
				<% } %>
				<section class="product-quantity">
					<h3>
						<label for="<%= productQuantityId %>"><%= xssAPI.filterHTML(i18n.get("Quantity")) %></label>
					</h3>
					<input name="product-quantity" id="<%= productQuantityId %>">
				</section>
			</div>
			<section class="product-submit">
				<input type="hidden" name="redirect"
					value="<%= resourceResolver.map(request, redirect) %>.html" /> <input
					type="hidden" name="redirect-product-not-found"
					value="<%= resourceResolver.map(request, errorRedirect) %>.html" />
				<input type="hidden" name="product-path"
					value="<%= product.getPath() %>" /> <span class="button-group"><input
					type="submit" value="<%= i18n.get("Add to Cart") %>" /><span></span></span>
			</section>
		</form>
		<form method="POST"
			action="<%= xssAPI.getValidHref(addToSmartListUrl) %>"
			onsubmit="return validateProductQuantity('<%= productQuantityId %>')"
			class="cq-cc-profile-not-anonymous">
			<section class="product-submit">
				<input type="hidden" name="redirect"
					value="<%= resourceResolver.map(request, smartListRedirect) %>.html" />
				<input type="hidden" name=":operation" value="addToSmartList">
				<input type="hidden" name="_charset_" value="utf-8"> <input
					type="hidden" name="product-path" value="<%= product.getPath() %>" />
				<p class="product-wishlist">
					<a class="default" href="#"><%= xssAPI.filterHTML(i18n.get("Add to Smart List")) %></a><a
						class="more" href="<%= product.getPath() %>.addtosmartlist.html">(+)</a>
				</p>
			</section>
		</form>


</article>
<%
        articleVisibility = "isHidden";
    }
%>
<br />
<br />
<br />

<div class="col-md-12" style="margin-top: 50px;">
	<ul id="myTab" class="nav nav-tabs nav_tabs">

		<li class="active"><a href="#service-one" data-toggle="tab">DESCRIPTION</a></li>
		<li><a href="#service-two" data-toggle="tab">PRODUCT INFO</a></li>
		<li><a href="#service-three" data-toggle="tab">REVIEWS</a></li>

	</ul>
	<div id="myTabContent" class="tab-content">
		<div class="tab-pane fade in active" id="service-one">

			<section class="container product-info"></section>

		</div>
		<div class="tab-pane fade" id="service-two">

			<section class="container"></section>

		</div>
		<div class="tab-pane fade" id="service-three"></div>
	</div>
	<hr>
</div>
</div>