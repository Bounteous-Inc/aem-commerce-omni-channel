package com.adobe.demo.wetelco.mobile.dps.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.slf4j.LoggerFactory;

import com.adobe.demo.wetelco.mobile.dps.AEMMobileDPSCatalogStyle;
import com.adobe.demo.wetelco.mobile.dps.AEMMobileDPSPage;
import com.adobe.demo.wetelco.mobile.dps.Constants;
import com.adobe.demo.wetelco.mobile.dps.operations.CreateArticleService;
import com.adobe.demo.wetelco.mobile.dps.operations.NodesHandler;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;

/**
 * Created by Daniel on 07/10/15.
 */
@Model(adaptables = Resource.class, adapters = {AEMMobileDPSCatalogStyle.class})
public class AEMMobileDPSCatalogStyleImpl implements AEMMobileDPSCatalogStyle {

    protected final org.slf4j.Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String NAME_SEPARATOR = "_";
    public static final String PRODUCTID_SEPARATOR = ".";
    private static final String COLLECTION_THUMBNAIL_PLACEHOLDER_PATH = "/content/dam/aem-mobile-dps/collection-thumbnail-placeholder.jpg";
    private static final String COLLECTION_BACKGROUND_PLACEHOLDER_PATH = "/content/dam/aem-mobile-dps/collection-background-placeholder.jpg";

    private static final String COLLECTION_THUMBNAIL_FILE_NAME = "Thumbnail.jpg";
    private static final String COLLECTION_BACKGROUND_FILE_NAME = "Background.jpg";

    @OSGiService
    NodesHandler nodesHandler;

    @OSGiService
    CreateArticleService createArticleService;

    @Inject @Source("sling-object")
    private ResourceResolver resourceResolver;

    @Inject @Named("uaDIO:type")
    String catalogDIOType;

    String styleNumber;

    Resource catalogTopLevelResource;

    Resource armouryResource;

    public AEMMobileDPSCatalogStyleImpl(Resource resource) {
        this.catalogResource = resource;
    }
    private final Resource catalogResource;

    @PostConstruct
    protected void init() {
        catalogTopLevelResource = nodesHandler.getCatalogTopLevelResource(catalogResource);
        styleNumber = catalogResource.getName();
        if (isArticle()) {
            armouryResource = _getArmouryResource();
        }
    }

    public boolean isTopLevelResource() {
        return StringUtils.equals(catalogResource.getPath(), catalogTopLevelResource.getPath());
    }

    public String getPath() {
        return catalogResource.getPath();
    }

    public String getSeason() {
        return catalogTopLevelResource.getValueMap().get(Constants.PROP_DIO_SEASON, "");
    }

    public String getRegion() {
        return catalogTopLevelResource.getValueMap().get(Constants.PROP_DIO_REGION, "");
    }

    public String getStyleNumber() {
        return styleNumber;
    }

    public boolean isArticle() {
        ValueMap vm = catalogResource.getValueMap();
        return vm.get(Constants.PROP_DIO_TYPE, "").equalsIgnoreCase(Constants.DIO_TYPE_ARTICLE);
    }

    public boolean isCollection() {
        final ValueMap vm = catalogResource.getValueMap();
        final String type = vm.get(Constants.PROP_DIO_TYPE, "");

        return type.equalsIgnoreCase(Constants.DIO_TYPE_CATALOG) ||
                type.equalsIgnoreCase(Constants.DIO_TYPE_CATEGORY_1) ||
                type.equalsIgnoreCase(Constants.DIO_TYPE_CATEGORY_2);
    }

    public String getName() {
        // Variant without backward compatibility
        // String name = StringUtils.join(getDPSNameParts(), NAME_SEPARATOR);
        // return DigestUtils.shaHex(name).substring(0, Constants.DPS_NAME_MAX_LENGTH);

        String name = StringUtils.join(getDPSNameParts(), NAME_SEPARATOR);
        if (name.length() > Constants.DPS_NAME_MAX_LENGTH) {
            name = DigestUtils.shaHex(name);
            if (name.length() > Constants.DPS_NAME_MAX_LENGTH) {
                name = name.substring(0, Constants.DPS_NAME_MAX_LENGTH);
            }
        }

        return name;
    }

    public String[] getDPSProductIds() {
        // Variant without backward compatibility
        //String productId = StringUtils.join(getDPSNameParts(), PRODUCTID_SEPARATOR);
        //return new String[]{DigestUtils.shaHex(productId).substring(0, Constants.DPS_PRODUCTID_MAX_LENGTH)};

        String productId = StringUtils.join(getDPSNameParts(), PRODUCTID_SEPARATOR);
        if (productId.length() > Constants.DPS_PRODUCTID_MAX_LENGTH) {
            productId = DigestUtils.shaHex(productId);
            if (productId.length() > Constants.DPS_PRODUCTID_MAX_LENGTH) {
                productId = productId.substring(0, Constants.DPS_PRODUCTID_MAX_LENGTH);
            }
        }

        return new String[]{productId};
    }

    public AEMMobileDPSCatalogStyle getParent() {
        if (isTopLevelResource()) {
            return null;
        } else {
            return catalogResource.getParent().adaptTo(AEMMobileDPSCatalogStyle.class);
        }
    }

    public AEMMobileDPSPage getDPSPage() {
        Page page = null;
        Node catalogNode = catalogResource.adaptTo(Node.class);
        try {
            Session session = catalogNode.getSession();

            if (isCollection()) {
                page = createArticleService.getCollectionPageIfExists(session, catalogNode);

            } else if (isArticle()) {
                page = createArticleService.getArticle(catalogResource);
            }

            if (page != null) {
                return page.adaptTo(AEMMobileDPSPage.class);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getTitle() {
        Resource[] parents = getInnerCatalogParents();
        List<String> ret = new ArrayList<String>();
        for (Resource res : parents) {
            ret.add(getSpecialTitle(res));
        }

        return StringUtils.join(ret, " ");
    }

    public String getDPSAbstract() {
        String prop = getArmouryProperty(Constants.PROP_DIO_STYLE_NAME, String.class);
        if (prop == null) {
            return StringUtils.EMPTY;
        }

        return prop;
    }

    public String getDPSCategory() {
        String prop = getArmouryProperty(Constants.PROP_DIO_GENDER, String.class);
        if (prop == null) {
            return StringUtils.EMPTY;
        }

        return prop;
    }

    public String getDPSDepartment() {
        final Set<String> allowedColorways = nodesHandler.getAllowedColorways(catalogResource).keySet();
        final String[] colorways = allowedColorways.toArray(new String[allowedColorways.size()]);
        if (colorways.length == 0) {
            return StringUtils.EMPTY;
        }
        String prop = getArmouryProperty(colorways[0] + "/" + getSeason() + "/" + Constants.PROP_DIO_DEPARTMENT, String.class);
        if (prop == null) {
            return StringUtils.EMPTY;
        }

        return prop;
    }

    public String getDPSEntityType() {
        if (isArticle()) {
            return Constants.DPS_TYPE_ARTICLE;
        } else if (isCollection()){
            return Constants.DPS_TYPE_COLLECTION;
        } else {
            return null;
        }
    }

    public String[] getDPSKeywords() {
        final List<Resource> parents = Arrays.asList(getInnerCatalogParents());
        final List<String> keywords = new ArrayList<String>();
        final ValueMap vm = catalogResource.getValueMap();


        for (Resource res : parents) {
            keywords.add(res.getName());
        }

        String type = vm.get(Constants.PROP_DIO_TYPE, "");
        if (StringUtils.isNotEmpty(type)) {
            keywords.add(type);
        }

        if (isArticle()) {
            boolean isNew = nodesHandler.isNewStyle(catalogResource);
            if (isNew) {
                keywords.add(Constants.PROP_KEYWORD_NEW);
            } else {
                keywords.add(Constants.PROP_KEYWORD_CARRYOVER);
            }
        }

        return keywords.toArray(new String[keywords.size()]);
    }

    public String getDPSLayoutName() {
        if (Constants.DIO_TYPE_CATALOG.equals(catalogDIOType)) {
            return "Division";
        } else if (Constants.DIO_TYPE_CATEGORY_1.equals(catalogDIOType) || Constants.DIO_TYPE_CATEGORY_2.equals(catalogDIOType)) {
            return "Division";
        }

        return null;
    }

    public String getBanner() {
        if (Constants.DIO_TYPE_CATALOG.equals(catalogDIOType)) {
            return "Spacer";
        } else if (Constants.DIO_TYPE_CATEGORY_1.equals(catalogDIOType) || Constants.DIO_TYPE_CATEGORY_2.equals(catalogDIOType)) {
            return "Spacer";
        }

        return null;
    }

    public String getDPSShortTitle() {
        return getSpecialTitle(catalogResource);
    }


    public String getArmouryPath() {
        return createArticleService.getDioArmouryPath() + "/style/" + styleNumber.substring(0, 3) + "/" + styleNumber.substring(0, 5) + "/" + styleNumber;
    }

    public String getArmouryLastModified() {
        String prop = getArmouryProperty(Constants.PROP_DIO_MOD_DATE, String.class);
        if (prop == null) {
            return StringUtils.EMPTY;
        }

        return prop;
    }

    public String getBackgroundPath()
            throws Exception {
        String thumbPath = COLLECTION_BACKGROUND_PLACEHOLDER_PATH;
        Node catalogNode = catalogResource.adaptTo(Node.class);

        if(catalogNode == null){
            return thumbPath;
        }

        final Session session = catalogNode.getSession();

        if (session.nodeExists(catalogNode.getPath() + "/" + COLLECTION_BACKGROUND_FILE_NAME)) {
            thumbPath = catalogNode.getPath() + "/" + COLLECTION_BACKGROUND_FILE_NAME;
        }


        return thumbPath;
    }


    public String getThumbnailPath()
            throws Exception {
        String thumbPath = COLLECTION_THUMBNAIL_PLACEHOLDER_PATH;
        Node catalogNode = catalogResource.adaptTo(Node.class);

        if(catalogNode == null){
            return thumbPath;
        }

        final Session session = catalogNode.getSession();

        if (session.nodeExists(catalogNode.getPath() + "/" + COLLECTION_THUMBNAIL_FILE_NAME)) {
            thumbPath = catalogNode.getPath() + "/" + COLLECTION_THUMBNAIL_FILE_NAME;
        }

        return thumbPath;
    }

    public Iterable<Resource> listChildren() {
        return catalogResource.getChildren();
    }

    /********************** PRIVATE *********************************/

    private List<String> getDPSNameParts() {
        List<String> ret = new ArrayList<String>();

        if (isArticle()) {
            ret.add(escapeNamePart(catalogTopLevelResource.getParent().getName()));
            ret.add(escapeNamePart(catalogTopLevelResource.getName()));
            ret.add(escapeNamePart(catalogResource.getName()));
        } else if (isCollection()) {
            final Resource[] parents = getInnerCatalogParents(true, true);
            for (Resource res : parents) {
                ret.add(escapeNamePart(res.getName()));
            }
        }

        return ret;
    }

    private String escapeNamePart(String val) {
        val = val.replaceAll("[^a-zA-Z0-9_.]", "_"); // replace forbidden characters with dash
        val = val.replaceAll("^[^a-zA-Z0-9]", "UA"); // make sure first character is always either letter or number
        val = val.replaceAll("[^a-zA-Z0-9]$", "UA"); // make sure last character is always either letter or number
        return val;
    }

    private Resource[] getInnerCatalogParents() {
        return getInnerCatalogParents(true, false);
    }

    private Resource[] getInnerCatalogParents(boolean includeStyleNode, boolean includeCatalogNode) {
        List<Resource> ret = new ArrayList<Resource>();
        Resource res;
        if (includeStyleNode) {
            res = catalogResource;
        } else {
            res = catalogResource.getParent();
        }
        String topLevelPath = catalogTopLevelResource.getPath();
        while (res != null && !res.getPath().equals(topLevelPath)) {
            ret.add(res);
            res = res.getParent();
        }
        ret.add(catalogTopLevelResource);

        if (includeCatalogNode) {
            ret.add(catalogTopLevelResource.getParent());
        }

        Collections.reverse(ret);
        return ret.toArray(new Resource[ret.size()]);
    }


    private <T> T getArmouryProperty(String name, Class<T> tClass) {
        if (armouryResource == null) {
            return null;
        }
        ValueMap vm = armouryResource.getValueMap();

        return vm.get(name, tClass);
    }

    private String getSpecialTitle(Resource res) {
        if (res == null) {
            return null;
        }
        Node node = res.adaptTo(Node.class);
        try {
            if (node.hasProperty(JcrConstants.JCR_TITLE) && StringUtils.isNotEmpty(node.getProperty(JcrConstants.JCR_TITLE).getString())) {
                return node.getProperty(JcrConstants.JCR_TITLE).getString();
            } else if (node.hasProperty(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE) &&
                    StringUtils.isNotEmpty(node.getProperty(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE).getString())) {
                return node.getProperty(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE).getString();
            } else {
                return node.getName();
            }
        } catch(Exception e) {
            return res.getName();
        }
    }

    private Resource _getArmouryResource() {
        final String armouryPath = getArmouryPath();
        if (StringUtils.isEmpty(armouryPath)) {
            return null;
        }
        return resourceResolver.getResource(armouryPath);
    }
}
