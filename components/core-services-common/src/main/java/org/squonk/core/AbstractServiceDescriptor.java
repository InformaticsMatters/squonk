package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.squonk.io.IODescriptor;
import org.squonk.options.OptionDescriptor;

import java.util.Date;

/**
 * Created by timbo on 04/01/17.
 */
public class AbstractServiceDescriptor implements ServiceDescriptor {

    protected final ServiceConfig serviceConfig;

    protected AbstractServiceDescriptor(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }


    protected AbstractServiceDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            ServiceConfig.Status status,
            Date statusLastChecked,
            IODescriptor[] inputDescriptors,
            IODescriptor[] outputDescriptors,
            OptionDescriptor[] optionDescriptors,
            String executorClassName) {

        this.serviceConfig = new ServiceConfig(id, name, description, tags, resourceUrl, icon,
                inputDescriptors, outputDescriptors, optionDescriptors, status, statusLastChecked, executorClassName);

    }

    @JsonIgnore
    @Override
    public String getId() {
        return serviceConfig.getId();
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }
}
