package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.io.IORoute;

import java.util.Map;


/** Service Descriptor for Nextflow execution.
 * These are best read from file in YAML format with a name of BASE_NAME.nsd.yml using the
 * @{link ServiceDescriptorUtils#readServiceDescriptor(String, Class)}
 * utility method that allows the nextflowFile and nextflowConfig properties to be declared in separate
 * files with the names BASE_NAME.nf and BASE_NAME.config (where BASE_NAME is the path to the .nsd.yml file).
 * If those properties are defined directly in the YAML these additional files are ignored.
 *
 * Created by timbo on 01/08/17.
 */
public class NextflowServiceDescriptor extends DefaultServiceDescriptor {

    private String nextflowFile;
    private String nextflowConfig;
    private Map<String,String> nextflowParams;

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
            @JsonProperty("nextflowFile") String nextflowFile,
            @JsonProperty("nextflowConfig") String nextflowConfig,
            @JsonProperty("nextflowParams") Map<String,String> nextflowParams) {
        super(serviceConfig, thinDescriptors, inputRoutes, outputRoutes);
        this.nextflowFile = nextflowFile;
        this.nextflowConfig = nextflowConfig;
        this.nextflowParams = nextflowParams;
    }

    public String getNextflowFile() {
        return nextflowFile;
    }

    public void setNextflowFile(String nextflowFile) {
        this.nextflowFile = nextflowFile;
    }

    public String getNextflowConfig() {
        return nextflowConfig;
    }

    public void setNextflowConfig(String nextflowConfig) {
        this.nextflowConfig = nextflowConfig;
    }

    public Map<String, String> getNextflowParams() {
        return nextflowParams;
    }
}
