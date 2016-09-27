package org.squonk.core.service.discovery;

import org.squonk.core.ServiceDescriptor;

import java.util.logging.Logger;

/**
 * @author timbo
 */
public class ServiceDescriptorUtils {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorUtils.class.getName());

    public static String makeAbsoluteUrl(String baseUrl, ServiceDescriptor serviceDescriptor) {
        if (serviceDescriptor.isEndpointRelative()) {
            if (baseUrl.endsWith("/")) {
                return baseUrl + serviceDescriptor.getExecutionEndpoint();
            } else {
                return baseUrl + "/" + serviceDescriptor.getExecutionEndpoint();
            }
        } else {
            return serviceDescriptor.getExecutionEndpoint();
        }
    }

    public static ServiceDescriptor makeAbsolute(String baseUrl, ServiceDescriptor serviceDescriptor) {

        if (serviceDescriptor.isEndpointRelative()) {
            return new ServiceDescriptor(
                    serviceDescriptor.getId(),
                    serviceDescriptor.getName(),
                    serviceDescriptor.getDescription(),
                    serviceDescriptor.getTags(),
                    serviceDescriptor.getResourceUrl(),
                    serviceDescriptor.getInputClass(),
                    serviceDescriptor.getOutputClass(),
                    serviceDescriptor.getInputType(),
                    serviceDescriptor.getOutputType(),
                    serviceDescriptor.getIcon(),
                    makeAbsoluteUrl(baseUrl, serviceDescriptor),
                    serviceDescriptor.isEndpointRelative(),
                    serviceDescriptor.getOptions(),
                    serviceDescriptor.getExecutorClassName()
            );
        } else {
            return serviceDescriptor;
        }

    }

}
