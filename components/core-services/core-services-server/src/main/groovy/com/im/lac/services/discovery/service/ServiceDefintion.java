package com.im.lac.services.discovery.service;

import com.im.lac.services.ServiceDescriptor;

/**
 *
 * @author timbo
 */
public class ServiceDefintion {

    private final String baseUrl;
    private final ServiceDescriptor[] serviceDescriptors;

    public ServiceDefintion(String baseUrl, ServiceDescriptor[] serviceDescriptors) {
        this.baseUrl = baseUrl;
        this.serviceDescriptors = serviceDescriptors;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ServiceDescriptor[] getServiceDesriptors() {
        return serviceDescriptors;
    }

}
