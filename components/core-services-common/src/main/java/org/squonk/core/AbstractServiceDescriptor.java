package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.dataset.ThinFieldDescriptor;
import org.squonk.io.IODescriptor;
import org.squonk.options.OptionDescriptor;

import java.util.Date;
import java.util.Map;

/**
 * Created by timbo on 04/01/17.
 */
public class AbstractServiceDescriptor implements ServiceDescriptor {

    final ServiceConfig serviceConfig;
    final ThinDescriptor[] thinDescriptors;

    protected AbstractServiceDescriptor(ServiceConfig serviceConfig, ThinDescriptor[] thinDescriptors) {
        this.serviceConfig = serviceConfig;
        this.thinDescriptors = thinDescriptors;
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
            ThinDescriptor[] thinDescriptors,
            String executorClassName) {

        this(new ServiceConfig(id, name, description, tags, resourceUrl, icon,
                inputDescriptors, outputDescriptors, optionDescriptors, status, statusLastChecked, executorClassName),
                thinDescriptors);


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


    public ThinDescriptor[] getThinDescriptors() {
        return thinDescriptors;
    }
}
