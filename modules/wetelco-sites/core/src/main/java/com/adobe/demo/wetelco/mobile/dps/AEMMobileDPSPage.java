package com.adobe.demo.wetelco.mobile.dps;

import java.util.Date;

import com.adobe.cq.mobile.dps.DPSProject;
import com.day.cq.wcm.api.Page;

public interface AEMMobileDPSPage {

    String getPath();

    Page getPage();

    AEMMobileDPSPage getParent();

    String getDpsId();

    DPSProject getDPSProject();

    boolean isUploaded();

    String getCatalogPath();

    AEMMobileDPSCatalogStyle getCatalogStyle();
    void setCatalogStyle(AEMMobileDPSCatalogStyle catalogStyle);

    String getArmouryLastModified();

    void setArmouryLastModified(String value);

    Date getLastPublished();

    String getLastPublishedBy();

    boolean isPublished();

    void updateDPSMetadata(AEMMobileDPSCatalogStyle catalogStyle);

    String getArticleCardImage();

    void setArticleCardImage(String thumbnailRef);

    void upload(boolean addToParentCollection);

    void publish(boolean publishParent);

    void unpublish();

    void delete();

    boolean isArticle();

    boolean isCollection();
}
