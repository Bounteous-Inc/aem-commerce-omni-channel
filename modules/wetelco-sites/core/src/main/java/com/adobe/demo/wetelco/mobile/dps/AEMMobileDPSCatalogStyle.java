package com.adobe.demo.wetelco.mobile.dps;

import org.apache.sling.api.resource.Resource;

public interface AEMMobileDPSCatalogStyle {
    boolean isTopLevelResource();

    String getPath();

    boolean isArticle();

    boolean isCollection();

    String getName();

    String[] getDPSProductIds();

    AEMMobileDPSCatalogStyle getParent();

    AEMMobileDPSPage getDPSPage();

    String getTitle();

    String getDPSAbstract();

    String getDPSCategory();

    String getDPSDepartment();

    String getDPSEntityType();

    String[] getDPSKeywords();

    String getDPSLayoutName();

    String getBanner();

    String getDPSShortTitle();

    String getBackgroundPath() throws Exception;

    String getThumbnailPath() throws Exception;

    Iterable<Resource> listChildren();
}
