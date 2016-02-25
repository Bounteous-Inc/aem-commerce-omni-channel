package com.adobe.demo.wetelco.mobile.dps.models;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;

import com.adobe.demo.wetelco.mobile.dps.DPSCatalogProject;
import com.day.cq.commons.jcr.JcrConstants;

@Model(adaptables = Resource.class, adapters = {DPSCatalogProject.class})
public class DPSCatalogProjectImpl implements DPSCatalogProject {
    public static final String ARTICLE_TEMPLATE = "/apps/aem-mobile-dps/templates/article";

    @Inject
    @Named(JcrConstants.JCR_CONTENT + "/" + "articleTemplate")
    @Default(values = ARTICLE_TEMPLATE)
    protected String articleTemplate;

    @Override
    public String getArticleTemplate() {
        return articleTemplate;
    }
}
