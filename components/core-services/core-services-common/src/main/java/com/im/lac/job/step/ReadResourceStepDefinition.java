package com.im.lac.job.step;

import java.net.URI;
import java.util.Map;

/**
 * Interface for a step that reads a resource and makes the results available to
 * the next step. It is expected that the source URI is either a file which can
 * be read or a http(s) URL that can be read with a GET operation.
 *
 * @author timbo
 */
public class ReadResourceStepDefinition extends StepDefinition {

    private URI sourceURI;
    private Map<String, Object> sourceParameters;

    public ReadResourceStepDefinition() {
    }

   public ReadResourceStepDefinition(URI sourceURI, Map<String, Object> sourceParameters) {
       configureSource(sourceURI, sourceParameters);
   }

    /**
     *
     * @return The URI from which the input can be read
     */
    public URI getSourceURI() {
        return sourceURI;
    }

    /**
     * Parameters for the invocation. The keys must start with header. or query.
     * to specify of they are query or header invocationParameters. Example: a
     * key of header.min_clusters specifies that a header name min_clusters
     * should be set with the corresponding value.
     *
     * @return the params
     */
    public Map<String, Object> getSourceParameters() {
        return sourceParameters;
    }

    public final void configureSource(URI sourceURI, Map<String, Object> sourceParameters) {
        this.sourceURI = sourceURI;
        this.sourceParameters = sourceParameters;
    }

}
