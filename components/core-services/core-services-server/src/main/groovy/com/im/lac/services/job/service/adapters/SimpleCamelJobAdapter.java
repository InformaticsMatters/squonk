package com.im.lac.services.job.service.adapters;

import com.im.lac.services.job.service.adapters.ProcessDatasetJobAdapter;
import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.job.JobExecutionException;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.services.job.service.AbstractDatasetJob;
import com.im.lac.services.job.service.JobHandler;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

/**
 * Adapter that sends its data to a camel route for processing
 *
 * @author timbo
 */
public class SimpleCamelJobAdapter extends ProcessDatasetJobAdapter {

    @Override
    public Object process(
            CamelContext context,
            AbstractDatasetJob job,
            ServiceDescriptor sd,
            String endpoint,
            JsonMetadataPair input,
            Map<String, Object> params)
            throws JobExecutionException {
        // first convert to objects
        Object objects = convertInput(context, sd, input);
        // now process
        Object results;
        try {
            results = submit(context, sd, endpoint, objects, params);
        } catch (Exception ex) {
            throw new JobExecutionException("Failed to execute camel route " + endpoint, ex);
        }
        return convertOutput(context, sd, results);
    }

    /**
     * Simple conversion involving unmarshaling the json. Override if you need something different.
     *
     * @param context
     * @param sd
     * @param input
     * @return
     * @throws JobExecutionException
     */
    Object convertInput(CamelContext context, ServiceDescriptor sd, JsonMetadataPair input) throws JobExecutionException {
        DatasetHandler datasetHandler = JobHandler.getDatasetHandler(context);
        try {
            return datasetHandler.generateObjectFromJson(input.getInputStream(), input.getMetadata());
        } catch (Exception ex) {
            throw new JobExecutionException("Failed to unmarshal json", ex);
        }
    }

    /**
     * Basic noop conversion returning the results unchanged. Override if you need something
     * different.
     *
     * @param context
     * @param sd
     * @param results
     * @return
     * @throws JobExecutionException
     */
    protected Object convertOutput(CamelContext context, ServiceDescriptor sd, Object results)
            throws JobExecutionException {
        return results;
    }

    /**
     * Simple submit - does not handle parameters. Override if you need to handle parameters or do
     * extra data conversions
     *
     * @param context
     * @param sd
     * @param endpoint
     * @param params
     * @param objects
     * @return
     * @throws Exception
     */
    public Object submit(
            CamelContext context,
            ServiceDescriptor sd,
            String endpoint,
            Object objects,
            Map<String, Object> params) throws Exception {
        ProducerTemplate pt = context.createProducerTemplate();
        return pt.requestBody(endpoint, objects);
    }

}
