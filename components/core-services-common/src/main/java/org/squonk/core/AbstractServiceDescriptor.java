package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.squonk.io.IODescriptor;
import org.squonk.options.OptionDescriptor;

import java.util.Date;

/**
 * Created by timbo on 04/01/17.
 */
public class AbstractServiceDescriptor implements ServiceDescriptor {

    private final ServiceConfig serviceConfig;


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

    @Override
    public String getId() {
        return serviceConfig.getId();
    }

    public String getName() {
        return serviceConfig.getName();
    }

    public String getDescription() {
        return serviceConfig.getDescription();
    }

    public String[] getTags() {
        return serviceConfig.getTags();
    }

    public String getResourceUrl() {
        return serviceConfig.getResourceUrl();
    }

    public String getIcon() {
        return serviceConfig.getIcon();
    }

    public IODescriptor[] getInputDescriptors() {
        return serviceConfig.getInputDescriptors();
    }

    public IODescriptor[] getOutputDescriptors() {
        return serviceConfig.getOutputDescriptors();
    }

    public OptionDescriptor[] getOptionDescriptors() {
        return serviceConfig.getOptionDescriptors();
    }

    @JsonIgnore
    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    @Override
    public String getExecutorClassName() {
        return serviceConfig.getExecutorClassName();
    }

    @Override
    public ServiceConfig.Status getStatus() {
        return serviceConfig.getStatus();
    }

    @Override
    public void setStatus(ServiceConfig.Status status) {
        serviceConfig.setStatus(status);
    }

    @Override
    public Date getStatusLastChecked() {
        return serviceConfig.getStatusLastChecked();
    }

    @Override
    public void setStatusLastChecked(Date statusLastChecked) {
        serviceConfig.setStatusLastChecked(statusLastChecked);
    }
}
