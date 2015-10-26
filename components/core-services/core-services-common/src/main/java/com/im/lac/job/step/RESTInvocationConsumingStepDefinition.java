package com.im.lac.job.step;

import java.net.URI;
import java.util.Map;

/**
 * Interface for a step that POSTs input to a URI to be processed and makes the
 * results available to the next step.
 *
 * @author timbo
 */
public class RESTInvocationConsumingStepDefinition extends ConsumingStepDefinition {

    private URI invocationURI;
    private Map<String, Object> invocationParameters;
    
    public RESTInvocationConsumingStepDefinition() {
        
    }
    
    public RESTInvocationConsumingStepDefinition(URI invocationURI, Map<String, Object> invocationParameters) {
        configureInvocation(invocationURI, invocationParameters);
    }

    /**
     * The HTTP endpoint to which the source content should be POSTed as the
     * body.
     *
     * @return
     */
    public URI getInvocationURI() {
        return invocationURI;
    }

    /**
     * Parameters for the invocation. The keys must start with header. or query.
     * to specify of they are query or header invocationParameters. Example: a
     * key of header.min_clusters specifies that a header name min_clusters
     * should be set with the corresponding value.
     *
     * @return the params
     */
    public Map<String, Object> getInvocationParameters() {
        return invocationParameters;
    }

    public final void configureInvocation(URI invocationURI, Map<String, Object> invocationParameters) {
        this.invocationURI = invocationURI;
        this.invocationParameters = invocationParameters;
    }

}
