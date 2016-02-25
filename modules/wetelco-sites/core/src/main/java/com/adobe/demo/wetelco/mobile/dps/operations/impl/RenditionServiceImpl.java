package com.adobe.demo.wetelco.mobile.dps.operations.impl;

import java.io.InputStream;

import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.gfx.Gfx;
import com.adobe.cq.gfx.Instructions;
import com.adobe.cq.gfx.Layer;
import com.adobe.cq.gfx.Plan;
import com.adobe.demo.wetelco.mobile.dps.operations.DPSCommon;
import com.adobe.demo.wetelco.mobile.dps.operations.RenditionsService;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.renditions.RenditionMaker;
import com.day.cq.dam.api.renditions.RenditionTemplate;
import com.day.cq.dam.api.thumbnail.ThumbnailConfig;
import com.day.cq.dam.commons.thumbnail.ThumbnailConfigImpl;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.dam.commons.util.OrientationUtil;

/**
 * Created by Daniel on 28/10/15.
 */
@Component( name="Renditions Generator for AEM Mobile DPS Articles",
        metatype = true, immediate = true)
@Property(name="service.description", value="Generate Renditions for AEM Mobile DPS Articles")
@Service
public class RenditionServiceImpl implements RenditionsService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static String REGENERATE = "regenerate";
    private static String MIME_TYPE = "image/jpeg";
    private static String RENDITION_430_430 = "430.430";

    @Reference
    private DPSCommon dpsCommon;

    @Reference
    private RenditionMaker renditionMaker;

    @Reference
    private Gfx gfx;

    @Override
    public Asset generateJpegRenditions(Session session, Resource resource, boolean regenerate) throws Exception {
        return generateJpegRenditions(resource, regenerate);
    }

    @Override
    public Asset generateJpegRenditions(Resource resource, boolean regenerate) throws Exception {
        Asset asset = DamUtil.resolveToAsset(resource);

        if(!dpsCommon.isValidImage(asset)){
            log.info("Invalid Image - " + (asset != null ? asset.getPath() : ""));
            return null;
        }

        ResourceResolver resolver = resource.getResourceResolver();
        Session session = resolver.adaptTo(Session.class);

        log.info("Generating JPEG renditions for asset - " + asset.getPath());

        if(!regenerate && getRendition(asset, RENDITION_430_430) != null){
            log.info("UA Renditions exists, not regenerating - " + asset.getPath());
            return null;
        }

        RenditionTemplate[] templates = createRenditionTemplates(asset);

        renditionMaker.generateRenditions(asset, templates);

        session.save();

        log.info("JPEG Renditions generation for asset complete - " + asset.getPath());
        return asset;
    }

    private Rendition getRendition(Asset asset, String rName) throws Exception{
        return asset.getRendition("UA." + rName + ".jpeg");
    }

    private RenditionTemplate[] createRenditionTemplates(Asset asset) {
        ThumbnailConfig[] thumbnails = new ThumbnailConfig[3];

        thumbnails[0] = new ThumbnailConfigImpl(430,430,true);
        thumbnails[1] = new ThumbnailConfigImpl(800,1000,true);
        thumbnails[2] = new ThumbnailConfigImpl(320,400,true);

        RenditionTemplate[] templates = new RenditionTemplate[thumbnails.length];

        for (int i = 0; i < thumbnails.length; i++) {
            ThumbnailConfig thumb = thumbnails[i];

            templates[i] = createThumbnailTemplate(asset, thumb.getWidth(), thumb.getHeight(), thumb.doCenter());
        }

        return templates;
    }

    private class JpegTemplate implements RenditionTemplate {
        public Plan plan;
        public String renditionName;
        public String mimeType;

        public Rendition apply(Asset asset) {
            InputStream stream = null;
            try {
                stream = gfx.render(plan, asset.adaptTo(Resource.class).getResourceResolver());
                if (stream != null) {
                    return asset.addRendition(renditionName, stream, mimeType);
                }
            } finally {
                IOUtils.closeQuietly(stream);
            }
            return null;
        }
    }

    private RenditionTemplate createThumbnailTemplate(Asset asset, int width, int height, boolean doCenter) {
        JpegTemplate template = new JpegTemplate();

        final Rendition rendition = asset.getOriginal();
        final boolean useRenditionPath = rendition.equals(asset.getOriginal());

        template.renditionName = "UA." + width + "." + height + ".jpeg";
        template.mimeType = MIME_TYPE;
        template.plan = gfx.createPlan();

        template.plan.layer(0).set("src", useRenditionPath ? rendition.getPath() : asset.getPath());

        applyOrientation(OrientationUtil.getOrientation(asset), template.plan.layer(0));

        applyThumbnail(width, height, doCenter, template.mimeType, template.plan);

        return template;
    }

    private static void applyThumbnail(int width, int height, boolean doCenter, String mimeType, Plan plan) {
        Instructions global = plan.view();

        global.set("wid", width);
        global.set("hei", height);

        // fit=fit will add whitespace (=doCenter) and result in exact target size, fit=constrain will not
        // upscaling is allowed (second flag "1")
        global.set("fit", doCenter ? "fit,1" : "constrain,1");

        // output format, ensure transparency gets written
        String fmt = StringUtils.substringAfter(mimeType, "/");
        if ("png".equals(fmt) || "gif".equals(fmt) || "tif".equals(fmt)) {
            fmt = fmt + "-alpha";
        }
        global.set("fmt", fmt);
    }

    private static void applyOrientation(short exifOrientation, Layer layer) {
        switch (exifOrientation) {
            case OrientationUtil.ORIENTATION_MIRROR_HORIZONTAL:
                layer.set("flip", "lr");
                break;
            case OrientationUtil.ORIENTATION_ROTATE_180:
                layer.set("rotate", 180);
                break;
            case OrientationUtil.ORIENTATION_MIRROR_VERTICAL:
                layer.set("flip", "ud");
                break;
            case OrientationUtil.ORIENTATION_MIRROR_HORIZONTAL_ROTATE_270_CW:
                layer.set("flip", "lr");
                layer.set("rotate", 270);
                break;
            case OrientationUtil.ORIENTATION_ROTATE_90_CW:
                layer.set("rotate", 90);
                break;
            case OrientationUtil.ORIENTATION_MIRROR_HORIZONTAL_ROTATE_90_CW:
                layer.set("flip", "lr");
                layer.set("rotate", 90);
                break;
            case OrientationUtil.ORIENTATION_ROTATE_270_CW:
                layer.set("rotate", 270);
                break;
        }
    }
}
