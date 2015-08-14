package com.im.lac.services.job.service.adapters;

import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.dataset.Metadata;
import com.im.lac.job.JobExecutionException;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.IncompatibleDataException;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.services.job.service.AbstractDatasetJob;
import com.im.lac.services.job.service.JobHandler;
import com.im.lac.services.job.service.AsyncJobRouteBuilder;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

/**
 *
 * @author timbo
 */
public class SimpleHttpJobAdapter extends ProcessDatasetJobAdapter {

    @Override
    public Object process(
            CamelContext context,
            AbstractDatasetJob job,
            ServiceDescriptor sd,
            String endpoint,
            JsonMetadataPair input,
            Map<String, Object> params) throws JobExecutionException {

        // first convert the data as needed
        InputStream converted = convertInput(context, sd, input);
        InputStream results = null;
        try {
            results = submit(context, job, sd, endpoint, converted, params);
            job.setStatus(JobStatus.Status.RESULTS_READY);
        } catch (Exception ex) {
            throw new JobExecutionException("Failed to post request to " + endpoint, ex);
        }

        Object objects = convertOutput(context, sd, results);
        return objects;
    }

    /**
     * Basic conversion involving unmarshaling the json. Override if you need to convert the output
     * before saving
     *
     * @param context
     * @param sd
     * @param results
     * @return
     * @throws com.im.lac.job.JobExecutionException
     */
    protected Object convertOutput(CamelContext context, ServiceDescriptor sd, InputStream results)
            throws JobExecutionException {
        // TODO - handle metadata in smart way. All we have is JSON so we don't 
        // know about any complex datatypes. Should the service return the metadata we can use?
        Metadata metadata = new Metadata(sd.getOutputClass().getName(), sd.getOutputType(), 0);
        DatasetHandler datasetHandler = JobHandler.getDatasetHandler(context);
        try {
            return datasetHandler.generateObjectFromJson(results, metadata);
        } catch (Exception ex) {
            throw new JobExecutionException("Failed to convert results to objects", ex);
        }
    }

    /**
     * Perform basic conversion of the input. Override if you need something different
     *
     * @param sd
     * @param input
     * @return
     * @throws JobExecutionException
     */
    InputStream convertInput(CamelContext context, ServiceDescriptor sd, JsonMetadataPair input) throws JobExecutionException {
        try {
            return JobHandler.convertData(sd, input);
        } catch (ClassNotFoundException | IncompatibleDataException ex) {
            throw new JobExecutionException("Failed to convert data", ex);
        }
    }

    /**
     * Simple submit - does not handle parameters. Override if you need to handle parameters or do
     * extra operations
     *
     * @param context
     * @param job
     * @param sd
     * @param endpoint
     * @param input
     * @return
     * @throws Exception
     */
    InputStream submit(
            CamelContext context,
            AbstractDatasetJob job,
            ServiceDescriptor sd,
            String endpoint,
            InputStream input,
            Map<String, Object> params) throws Exception {
        //return JobHandler.postRequest(endpoint, input);
        
        ProducerTemplate pt = context.createProducerTemplate();
        Map<String, Object> headers = new HashMap();
        headers.put(Exchange.HTTP_URI, endpoint);
        headers.put(Exchange.HTTP_METHOD, "POST");
        
        return pt.requestBodyAndHeaders(AsyncJobRouteBuilder.ROUTE_HTTP_SUBMIT, input, headers, InputStream.class);
    }

}
