package com.im.lac.portal.webapp;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;

public class ExternalStructureImage extends WebComponent {

    private String key;
    private String resourceName;
    private String datasetId;

    public ExternalStructureImage(String id, String key, String datasetId, String resourceName) {
        super(id);
        this.key = key;
        this.resourceName = resourceName;
        this.datasetId = datasetId;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        ResourceReference resource = new SharedResourceReference(resourceName);
        PageParameters pageParameters = new PageParameters();
        pageParameters.add("rowIdAsString", key);
        pageParameters.add("datasetIdAsString", datasetId);
        CharSequence url = RequestCycle.get().urlFor(resource, pageParameters);
        if(key != null && key.trim().length() > 0) {
            tag.put("src", url);
        }
    }

}

