package org.squonk.core.service.discovery;

import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;

/**
 *
 * @author timbo
 */
public class ServiceDescriptorUtils {

    public static String getAbsoluteUrl(String baseUrl, AccessMode mode) {
        if (mode.isEndpointRelative()) {
            if (baseUrl.endsWith("/")) {
                return baseUrl + mode.getExecutionEndpoint();
            } else {
                return baseUrl + "/" + mode.getExecutionEndpoint();
            }
        } else {
            return mode.getExecutionEndpoint();
        }
    }

    public static ServiceDescriptor makeAbsolute(String baseUrl, ServiceDescriptor serviceDescriptor) {
        AccessMode[] modes = new AccessMode[serviceDescriptor.getAccessModes().length];
        for (int i = 0; i < modes.length; i++) {
            modes[0] = makeAbsolute(baseUrl, serviceDescriptor.getAccessModes()[i]);
        }
        return new ServiceDescriptor(serviceDescriptor.getId(), serviceDescriptor.getName(), serviceDescriptor.getDescription(),
                serviceDescriptor.getTags(), serviceDescriptor.getResourceUrl(), serviceDescriptor.getPaths(),
                serviceDescriptor.getOwner(), serviceDescriptor.getOwnerUrl(),
                serviceDescriptor.getLayers(),
                serviceDescriptor.getInputClass(), serviceDescriptor.getOutputClass(), serviceDescriptor.getInputType(), serviceDescriptor.getOutputType(),
                modes
        );

    }

    public static AccessMode makeAbsolute(String baseUrl, AccessMode mode) {
        if (!mode.isEndpointRelative()) {
            return mode;
        }
        String absoluteUrl = getAbsoluteUrl(baseUrl, mode);
        return new AccessMode(mode.getId(), mode.getName(), mode.getDescription(),
                absoluteUrl, false, // these are new - all else are the original values
                mode.getJobType(), mode.getMinSize(), mode.getMaxSize(), mode.getCost(),
                mode.getRequiredLicenseTokens(), mode.getParameters(), mode.getAdapterClassName()
        );
    }

    public static AccessMode findAccessMode(ServiceDescriptor serviceDescriptor, String accessModeId) {
        for (AccessMode mode : serviceDescriptor.getAccessModes()) {
            if (accessModeId.equals(mode.getId())) {
                return mode;
            }
        }
        return null;
    }
}
