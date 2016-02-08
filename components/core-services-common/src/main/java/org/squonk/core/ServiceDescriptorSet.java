package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Definition of set of ServiceDescriptors. Each set is defined at a specific URL, with the
 * individual ServiceDescriptor endpoint possibly being relative to this base URL. The
 * {@link AccessMode.endpointIsRelative()} method defined whether the defined endpoint is absolute or
 * relative.
 * TODO - not clear exactly how this will work for message queue endpoints. Will they all have to
 * be absolute?
 *
 * @author timbo
 */
public class ServiceDescriptorSet implements Serializable {

    private final String baseUrl;
    private final ServiceDescriptor[] serviceDescriptors;

    public ServiceDescriptorSet(
            @JsonProperty("baseUrl") String baseUrl,
            @JsonProperty("serviceDescriptors") ServiceDescriptor[] serviceDescriptors) {
        this.baseUrl = baseUrl;
        this.serviceDescriptors = serviceDescriptors;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ServiceDescriptor[] getServiceDescriptors() {
        return serviceDescriptors;
    }

}
