package apps.we_retail_instore.components.page_catalog;

import java.io.StringWriter;
import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.commerce.api.CommerceException;
import com.adobe.cq.commerce.api.Product;
import com.adobe.cq.sightly.WCMUse;
import com.day.cq.commons.ImageResource;
import com.day.cq.wcm.api.Page;

public class ScreensCatalog extends WCMUse {

    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(ScreensCatalog.class);

    @Override
    public void activate() throws Exception {
    }

    public String getJSON() throws JSONException, CommerceException {
        Page page = getCurrentPage();
        StringWriter out = new StringWriter();
        JSONWriter w = new JSONWriter(out);
        dumpJson(w, page);
        return out.toString();
    }

    private void dumpJson(JSONWriter w, Page page) throws JSONException, CommerceException {
        ValueMap props = page.getProperties();

        w.object();
        w.key("path").value(page.getPath());
        Resource product = page.getContentResource("product");
        Product p = product == null ? null : product.adaptTo(Product.class);
        if (p != null) {
            w.key("title").value(p.getTitle());
            Product pimProduct = p.getPIMProduct();
            if (pimProduct != null) {
                w.key("productPath").value(pimProduct.getPath());
            }
            // rating
            Double rating = p.getProperty("rating", Double.class);
            Double ratingCount = p.getProperty("ratingCount", Double.class);
            w.key("rating").value(rating == null ? 0 : rating);
            w.key("ratingCount").value(ratingCount == null ? 0 : ratingCount);

            ImageResource img = p.getImage();
            w.key("imageHref").value(img.getHref());
        } else {
            w.key("title").value(page.getTitle());
            w.key("coverImage").value(props.get("coverImage", null));
            w.key("callouts").array();
            Resource calloutsResource = page.getContentResource("callouts");
            Iterator<Resource> calloutsIterator = calloutsResource == null ? null : calloutsResource.listChildren();
            if (calloutsIterator != null) {
                ValueMap callout;
                String[] values;
                while (calloutsIterator.hasNext()) {
                    callout = calloutsIterator.next().adaptTo(ValueMap.class);
                    if (callout.get("product", String.class) != null) {
                        w.object();
                        w.key("product").value(callout.get("product", String.class));
                        w.key("position").array();
                        values = callout.get("position", String[].class);
                        for (int i = 0; i < values.length; i++) {
                            w.value(Double.parseDouble(values[i]));
                        }
                        w.endArray();
                        w.key("buttonPosition").array();
                        values = callout.get("buttonPosition", String[].class);
                        for (int i = 0; i < values.length; i++) {
                            w.value(Double.parseDouble(values[i]));
                        }
                        w.endArray();
                        w.endObject();
                    }
                }
            }
            w.endArray();

            Iterator<Page> pages = page.listChildren();
            if (pages.hasNext()) {
                w.key("children").array();
                while (pages.hasNext()) {
                    dumpJson(w, pages.next());
                }
                w.endArray();
            }
        }
        w.endObject();
    }
}
