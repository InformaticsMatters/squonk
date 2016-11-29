package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.json.PackageVersion;

import java.io.Serializable;
import java.util.*;

/**
 * Definition of set of ServiceDescriptors. Each set is defined at a specific URL, with the
 * individual ServiceDescriptor endpoint possibly being relative to this base URL. The
 * {@link org.squonk.core.ServiceDescriptor#isEndpointRelative} method defined whether the defined endpoint is absolute or
 * relative.
 *
 *
 * @author timbo
 */
public class ServiceDescriptorSet implements Serializable {

    private final String baseUrl;
    private final String healthUrl;
    private final Map<String,ServiceDescriptor> serviceDescriptorsMap = new LinkedHashMap<>();

    public ServiceDescriptorSet(
            @JsonProperty("baseUrl") String baseUrl,
            @JsonProperty("healthUrl") String healthUrl,
            @JsonProperty("serviceDescriptors") List<ServiceDescriptor> serviceDescriptors) {
        this.baseUrl = baseUrl;
        this.healthUrl = healthUrl;
        serviceDescriptors.forEach(sd -> {
            serviceDescriptorsMap.put(sd.getId(), sd);
        });
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /** Get the absolute URL that can be used to test if these services are running healthily.
     * If a GET operation on this URL gives a 200 response then all services are considered healthy.
     *
     * @return
     */
    public String getHealthUrl() {
        return healthUrl;
    }

    public List<ServiceDescriptor> getServiceDescriptors() {
        List<ServiceDescriptor> list = new ArrayList<>();
        list.addAll(serviceDescriptorsMap.values());
        return list;
    }

    public void updateServiceDescriptors(List<ServiceDescriptor> sds) {
        sds.forEach(sd -> serviceDescriptorsMap.put(sd.getId(), sd));
    }

    public void removeServiceDescriptor(String id) {
        serviceDescriptorsMap.remove(id);
    }

}
