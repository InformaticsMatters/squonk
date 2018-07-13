/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.services.camel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.ExecutionParameters;
import org.squonk.execution.JobManager;
import org.squonk.jobdef.JobStatus;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;

import javax.activation.DataHandler;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * External execution of squonk services as a job through a REST API
 */
public class JobExecutorRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(JobExecutorRouteBuilder.class.getName());
    private static final String ROUTE_STATS = "seda:post_stats";

    @Inject
    private JobManager jobManager;

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");


        /* These are the REST endpoints - exposed as public web services
         */

        rest("/ping").description("Simple ping service to check things are running")
                .get()
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();

        rest("/v1/jobs").description("Property calculation services using RDKit")
                //
                // job status
                .get("/{id}").description("Get the latest status of an individual job")
                .bindingMode(RestBindingMode.off)
                .produces(CommonMimeTypes.MIME_TYPE_JSON)
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    String id = exch.getIn().getHeader("id", String.class);
                    LOG.info("Fetching Job " + id);
                    JobStatus jobStatus = jobManager.getJobStatus(id);
                    LOG.info("Fetched Job " + jobStatus);
                    if (jobStatus == null) {
                        exch.getIn().setBody("{ \"error\": \"Job not found\"}");
                        exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                    } else {
                        String json = JsonHandler.getInstance().objectToJson(jobStatus);
                        exch.getIn().setBody(json);
                    }
                })
                .endRest()
                // submit new job
                .post("/").description("Submit a new job")
                .bindingMode(RestBindingMode.off)
                .outType(JobStatus.class)
                .route()
                .process((Exchange exch) -> {
                    handleJobSubmit(exch);
                })
                .endRest()
        // need to handle terminate. DELETE op?
        // need to handle getData()
        // need to handle cleanup()
        ;
    }

    private void handleJobSubmit(Exchange exch) {
        try {
            JobStatus jobStatus = doHandleJobSubmit(exch.getIn());
            String json = JsonHandler.getInstance().objectToJson(jobStatus);
            exch.getIn().setBody(json);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to submit job", e);
            exch.getIn().setBody("{ \"error\": \"Job failed to submit: " + e.getLocalizedMessage() + "\"}");
            exch.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
        }
    }

    private JobStatus doHandleJobSubmit(Message message) throws Exception {

        String body = message.getBody(String.class);
        ExecutionParameters params = JsonHandler.getInstance().objectFromJson(body, ExecutionParameters.class);
        ServiceDescriptor serviceDescriptor = params.getServiceDescriptor();
        Map<String, Object> options = params.getOptions();
        Map<String, InputStream> inputs = new HashMap<>();
        String username = message.getHeader("SquonkUsername", String.class);
        for (Map.Entry<String, DataHandler> e : message.getAttachments().entrySet()) {
            String name = e.getKey();
            InputStream input = e.getValue().getInputStream();
            inputs.put(name, input);
        }

        return jobManager.executeAsync(serviceDescriptor, options, inputs, username);
    }


}
