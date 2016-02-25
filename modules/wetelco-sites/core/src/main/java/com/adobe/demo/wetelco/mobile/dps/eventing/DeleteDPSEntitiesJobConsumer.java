package com.adobe.demo.wetelco.mobile.dps.eventing;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.demo.wetelco.mobile.dps.operations.CreateArticleService;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Component(
        label = "AEM Mobile DPS DPS Publish Job Consumer",
        description = "AEM Mobile DPS DPS Publish Sling Job Consumer",
        immediate = true
)
@Properties({
        @Property(
                label = "Job Topics",
                value = {DeleteDPSEntitiesJobConsumer.JOB_TOPIC},
                description = "[Required] Job Topics this job consumer will to respond to.",
                name = JobConsumer.PROPERTY_TOPICS,
                propertyPrivate = true
        )
})
@Service
public class DeleteDPSEntitiesJobConsumer implements JobConsumer {
    private static final Logger log = LoggerFactory.getLogger(DeleteDPSEntitiesJobConsumer.class);

    public static final String JOB_TOPIC = "com/adobe/gss/aem/mobile/dps/delete";
    public static final String PAGE_PATH = "PAGE_PATH";


    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Reference
    CreateArticleService createArticleService;

    @Override
    public JobResult process(final Job job) {
        ResourceResolver resolver = null;

        try {
            resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

            String page = (String) job.getProperty(PAGE_PATH);

            PageManager pageManager = resolver.adaptTo(PageManager.class);
            Page dpsPage = pageManager.getPage(page);

            if(dpsPage == null){
                log.warn("PAGE DOES NOT EXIST - " + page);
                return JobResult.OK;
            }

            if (createArticleService.isEntityPublished(dpsPage)) {
                createArticleService.unpublish(dpsPage);
                return JobResult.FAILED;
            }

            createArticleService.dpsDelete(dpsPage);
        }catch(Exception e){
            log.error("ERROR PERFORMING DPS DELETE JOB", e);
        }finally{
            if(resolver != null){
                resolver.close();
            }
        }

        return JobResult.FAILED;
    }

    private boolean isFirstTime(Job job){
        return (job.getRetryCount() == 0);
    }
}
