package org.squonk.core.service.discovery;

import org.squonk.core.HttpServiceDescriptor;

import java.util.logging.Logger;

/**
 * @author timbo
 */
public class ServiceDescriptorUtils {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorUtils.class.getName());

    protected static String makeAbsoluteUrl(String baseUrl, HttpServiceDescriptor httpHttpServiceDescriptor) {
        String endpoint = httpHttpServiceDescriptor.getExecutionEndpoint();
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

    public static HttpServiceDescriptor makeAbsolute(String baseUrl, HttpServiceDescriptor httpHttpServiceDescriptor) {

        String endpoint = httpHttpServiceDescriptor.getExecutionEndpoint();
        if (endpoint == null) {
            return httpHttpServiceDescriptor;
        } else {

            if (!isAbsoluteUrl(endpoint)) {
                return new HttpServiceDescriptor(
                        httpHttpServiceDescriptor.getId(),
                        httpHttpServiceDescriptor.getName(),
                        httpHttpServiceDescriptor.getDescription(),
                        httpHttpServiceDescriptor.getTags(),
                        httpHttpServiceDescriptor.getResourceUrl(),
                        httpHttpServiceDescriptor.getIcon(),
                        httpHttpServiceDescriptor.getStatus(),
                        httpHttpServiceDescriptor.getStatusLastChecked(),
                        httpHttpServiceDescriptor.getInputDescriptors(),
                        httpHttpServiceDescriptor.getOutputDescriptors(),
                        httpHttpServiceDescriptor.getOptionDescriptors(),
                        httpHttpServiceDescriptor.getExecutorClassName(),
                        makeAbsoluteUrl(baseUrl, httpHttpServiceDescriptor)
                );
            } else {
                return httpHttpServiceDescriptor;
            }
        }

    }

}
