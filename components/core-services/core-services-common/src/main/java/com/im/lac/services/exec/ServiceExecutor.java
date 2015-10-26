package com.im.lac.services.exec;

import java.util.Map;

/**
 * Base class for service execution
 *
 * @author timbo
 */
public class ServiceExecutor {

    /**
     * The HTTP endpoint to which the content should be POSTed as the body.
     */
    private String endpoint;

    /**
     * Parameters for the invocation. The keys must start with .header or .query
     * to specify of they are query or header invocationParameters. Example: a
     * key of header.min_clusters specifies that a header name min_clusters
     * should be set with the corresponding value.
     */
    private Map<String, String> invocationParameters;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Map<String, String> getInvocationParameters() {
        return invocationParameters;
    }

    public void setInvocationParameters(Map<String, String> parameters) {
        this.invocationParameters = parameters;
    }

}
