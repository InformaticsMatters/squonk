package org.squonk.core.service.discovery;

import org.squonk.core.ServiceDescriptor;

import java.util.logging.Logger;

/**
 * @author timbo
 */
public class ServiceDescriptorUtils {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorUtils.class.getName());

    protected static String makeAbsoluteUrl(String baseUrl, ServiceDescriptor serviceDescriptor) {
        String endpoint = serviceDescriptor.getExecutionEndpoint();
        if (endpoint == null) {
            endpoint = "";
        } else if (isAbsoluteUrl(endpoint)) {
            return endpoint;
        }

        if (baseUrl.endsWith("/")) {
            return baseUrl + endpoint;
        } else {
            return baseUrl + "/" + endpoint;
        }
    }

    private static boolean isAbsoluteUrl(String url) {
        return url.toLowerCase().startsWith("http:") || url.toLowerCase().startsWith("https:");
    }

    public static ServiceDescriptor makeAbsolute(String baseUrl, ServiceDescriptor serviceDescriptor) {

        String endpoint = serviceDescriptor.getExecutionEndpoint();
        if (endpoint == null) {
            return serviceDescriptor;
        } else {

            if (!isAbsoluteUrl(endpoint)) {
                return new ServiceDescriptor(
                        serviceDescriptor.getId(),
                        serviceDescriptor.getName(),
                        serviceDescriptor.getDescription(),
                        serviceDescriptor.getTags(),
                        serviceDescriptor.getResourceUrl(),
                        serviceDescriptor.getIcon(),
                        serviceDescriptor.getStatus(),
                        serviceDescriptor.getStatusLastChecked(),
                        serviceDescriptor.getInputDescriptors(),
                        serviceDescriptor.getOutputDescriptors(),
                        serviceDescriptor.getOptionDescriptors(),
                        serviceDescriptor.getExecutorClassName(),
                        makeAbsoluteUrl(baseUrl, serviceDescriptor)
                );
            } else {
                return serviceDescriptor;
            }
        }

    }

}
