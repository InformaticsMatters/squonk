package com.im.lac.services.job.service.adapters;

import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.job.JobExecutionException;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.job.service.AbstractDatasetJob;
import java.util.Map;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public abstract class ProcessDatasetJobAdapter {

    public abstract Object process(CamelContext context, AbstractDatasetJob job, ServiceDescriptor sd, String endpoint, JsonMetadataPair input, Map<String,Object> params) throws JobExecutionException;

}
