package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.io.IORoute;


/**
 * Created by timbo on 01/08/17.
 */
public class NextflowServiceDescriptor extends DefaultServiceDescriptor {

    private final String nextflowFile;

    /**
     *
     * @param serviceConfig
     * @param inputRoutes
     * @param outputRoutes
     */
    @JsonCreator
    public NextflowServiceDescriptor(
            @JsonProperty("serviceConfig") ServiceConfig serviceConfig,
            @JsonProperty("thinDescriptors") ThinDescriptor[] thinDescriptors,
            @JsonProperty("inputRoutes") IORoute[] inputRoutes,
            @JsonProperty("outputRoutes") IORoute[] outputRoutes,
            @JsonProperty("nextflowFile") String nextflowFile) {
        super(serviceConfig, thinDescriptors, inputRoutes, outputRoutes);
        this.nextflowFile = nextflowFile;
    }

    public String getNextflowFile() {
        return nextflowFile;
    }
}
