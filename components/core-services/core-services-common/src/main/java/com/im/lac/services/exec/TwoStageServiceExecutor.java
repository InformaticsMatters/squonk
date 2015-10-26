package com.im.lac.services.exec;

import java.util.Map;

/**
 * Service executor that work by creating a new resource endpoint that services
 * the actual request. That endpoint is created by POSTing the request to the
 * endpoint defined by this service executor. Execution proceeds as follows:
 * <ol>
 * <li>The content of the body property is POSTed to the endpoint defined by the
 * endpoint property (e.g. http://some.url/foo/service1) along with the header 
 * and/or query parameters defined by the creationParameters property.</li>
 * <li>This result of this POST is JSON representing an InvocationEndoint class that
 * defines the new resource/endpoint that can be invoked. e.g. http://some.url/foo/service1/XYZ123
 * and, optionally, additional resources can be retrieved e.g. an endpoint can provide 
 * metrics and metadata that can be retrieved after execution of the POST</li>
 * <li>Execution of the service is done by POSTing the content as the body to
 * the generated endpoint along with the execution parameters.</li>
 * <li>The result is the content to be returned or further processed.</li>
 * <li>Once processing is complete the endpoint can be called with GET operations 
 * to retrieve the additional information with the required option as a query parameter
 * e.g. http://some.url/foo/service1/XYZ123?metadata</li>
 * <li>Finally the generated endpoint is DELETEd to clean up.</li>
 * </ol>
 *
 * @author timbo
 */
public class TwoStageServiceExecutor extends ServiceExecutor {

    /**
     * These parameters are similar to the invocation parameters but are used 
     * when the endpoint to be called is created. They can be used to define the 
     * function of the endpoint, or to provide default parameters that can be overridden 
     * by the invocation parameters
     */
    Map<String, String> creationParameters;

    /**
     * The body that is posted to the endpoint to generate the new endpoint to
     * invoke. For instance, this can be a Molfile containing a query structure.
     *
     */
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getCreationParameters() {
        return creationParameters;
    }

    public void setCreationParameters(Map<String, String> parameters) {
        this.creationParameters = parameters;
    }

}
