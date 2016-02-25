package com.adobe.demo.wetelco.mobile.dps;

import java.text.SimpleDateFormat;

public final class Constants {

	private static final String DIO_PROP_PREFIX = "uaDIO:";

	public static final String PROP_DIO_TYPE = DIO_PROP_PREFIX + "type";
	public static final String PROP_DIO_SEASON = DIO_PROP_PREFIX + "season";
	public static final String PROP_DIO_REGION = DIO_PROP_PREFIX + "region";
	public static final String PROP_DIO_LOCALE = DIO_PROP_PREFIX + "locale";
	public static final String PROP_DIO_IMAGE_URL = DIO_PROP_PREFIX + "imageURL";
	public static final String PROP_DIO_PRODUCT_LIFECYCLE = DIO_PROP_PREFIX + "productLifecycle";
	public static final String PROP_DIO_COLORWAY_LIFECYCLE = DIO_PROP_PREFIX + "colorwayLifecycle";
	public static final String PROP_DIO_MARKETING_NAME = DIO_PROP_PREFIX + "marketingName";
	public static final String PROP_DIO_STYLE_NAME = DIO_PROP_PREFIX + "styleName";
	public static final String PROP_DIO_MOD_DATE = DIO_PROP_PREFIX + "modified";
	public static final String PROP_DIO_GENDER = DIO_PROP_PREFIX + "gender";
	public static final String PROP_DIO_DEPARTMENT = DIO_PROP_PREFIX + "department";

	public static final String PROP_PRODUCT_LIFECYCLE_NEW = "New";

	public static final String PROP_KEYWORD_NEW = "New";
	public static final String PROP_KEYWORD_CARRYOVER = "Carryover";

	public static final String DIO_TYPE_CATALOG = "catalog-node";
	public static final String DIO_TYPE_CATEGORY_1 = "category1-node";
	public static final String DIO_TYPE_CATEGORY_2 = "category2-node";
	public static final String DIO_TYPE_ARTICLE = "article-node";

	public static final String DPS_TYPE_ARTICLE = "article";
	public static final String DPS_TYPE_COLLECTION = "collection";

	public static final String DIO_TYPE_ARTICLE_CONTENT = "article-content-node";

	public static final String DPS_DEFAULT_LAYOUT_NAME = "defaultLayout";

	public static final String NN_ARTICLES = "articles";
	public static final String NN_COLLECTIONS = "collections";

	public static final String DIO_STYLE = "dioStyle";

	public static final String REND_430_430 = "UA.430.430.jpeg";
	public static final String THUMBNAIL = "Thumbnail.jpg";
	public static final String BACKGROUND = "Background.jpg";

	public static final String STYLE_LOCATION = "/etc/dio-armoury/style";
	public static final String STYLE_STATUS = DIO_PROP_PREFIX + "styleStatus";
	public static final String CW_STATUS = DIO_PROP_PREFIX + "status";

	public static final String ACTIVE = "Active";
	public static final String DROPPED = "Dropped";

	public static final String PROP_DPS_ID = "dps-id";
	public static final String PROP_DPS_KEYWORDS = "dps-keywords";
	public static final String PROP_DPS_ABSTRACT = "dps-abstract";
	public static final String PROP_DPS_CATEGORY = "dps-category";
	public static final String PROP_DPS_DEPARTMENT = "dps-department";
	public static final String PROP_DPS_SHORT_TITLE = "dps-shortTitle";
	public static final String PROP_DPS_TITLE = "dps-title";
	public static final String PROP_DPS_ENTITY_TYPE = "dps-entityType";
	public static final String PROP_DPS_PRODUCT_IDS = "dps-productIds";
	public static final String PROP_DPS_PRODUCT_ID = "dps-productId";
	public static final String PROP_DPS_LAST_UPLOADED = "dps-lastUploaded";

	public static final String PROP_NAME = "name";

	public static final String[] ARMOURY_DATE_FORMATS = { "M/dd/yyyy hh:mm:ss", "M/dd/yyyy hh:mm:ss a",
			"M/d/yyyy hh:mm:ss a" };
	public static final String DPS_TOP_LEVEL_COLLECTION_NAME = "topLevelContent";

	public static final int DPS_NAME_MAX_LENGTH = 64;
	public static final int DPS_PRODUCTID_MAX_LENGTH = 100;

	public static final SimpleDateFormat DPS_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

	public static final String IMAGE = "image";
	public static final String BACKGROUND_IMAGE = "background-image";

	public static enum DPS_PUBLISH_STATUS {
		NEW, PUBLISHED, PUBLISHED_OUT_OF_DATE, UNPUBLISHED
	}
}
