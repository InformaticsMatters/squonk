package com.im.lac.job.step;

import java.net.URI;
import java.util.Map;

/**
 * Interface for a step that writes results to a resource. It is expected that
 * the URI is either a file which can be written or a http(s) URL that can be
 * POSTed to.
 *
 * @author timbo
 */
public class WriteResourceStepDefinition extends ConsumingStepDefinition {

    private URI destinationURI;
    private Map<String, Object> destinationParameters;

    public WriteResourceStepDefinition() {

    }

    public WriteResourceStepDefinition(URI destinationURI, Map<String, Object> destinationParameters) {
        configureDestination(destinationURI, destinationParameters);
    }

    /**
     *
     * @return The URI from which the input can be read
     */
    public URI getDestinationURI() {
        return destinationURI;
    }

    /**
     * Parameters for the invocation. The keys must start with header. or query.
     * to specify if they are header or query params.
     *
     * @return the params
     */
    public Map<String, Object> getDestinationParameters() {
        return destinationParameters;
    }

    public final void configureDestination(URI destinationURI, Map<String, Object> destinationParameters) {
        this.destinationURI = destinationURI;
        this.destinationParameters = destinationParameters;
    }

}
