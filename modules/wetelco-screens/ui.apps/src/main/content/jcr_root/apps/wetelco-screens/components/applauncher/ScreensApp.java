package apps.we_retail_instore.components.applauncher;

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

public class ScreensApp extends WCMUse {

    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(ScreensApp.class);

    @Override
    public void activate() throws Exception {
    }

    public String getAppPath() {
        return getResourcePage().getPath();
    }

    public String getDevicePath() {
        return getCurrentPage().getPath();
    }

    public String getDisplayPath() {
        return getCurrentPage().getParent().getPath();
    }
}
