package com.im.lac.services.impl;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.services.ServiceDescriptor;

/**
 *
 * @author timbo
 */
public class ModeImpl implements ServiceDescriptor.Mode {
    private final String name;
    private final String description;
    private final String executionEndpoint;
    private final Class<? extends JobDefinition> jobType;
    private final int minSize;
    private final int maxSize;
    private final float cost;
    private final ServiceDescriptor.LicenseToken[] requiredLienceTokens;


    public ModeImpl(String name, String description, String executionEndpoint, Class<? extends JobDefinition> jobType, int minSize, int maxSize, float cost, ServiceDescriptor.LicenseToken[] requiredLienceTokens) {
        
        assert cost >= 0;
        
        this.name = name;
        this.description = description;
        this.executionEndpoint = executionEndpoint;
        this.jobType = jobType;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.cost = cost;
        this.requiredLienceTokens = requiredLienceTokens;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getExecutionEndpoint() {
        return executionEndpoint;
    }

    @Override
    public Class<? extends JobDefinition> getJobType() {
        return jobType;
    }

    @Override
    public int getMinSize() {
        return minSize;
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public ServiceDescriptor.LicenseToken[] getRequiredLienceTokens() {
        return requiredLienceTokens;
    }

    @Override
    public Object[] getParameters() {
        return null;
    }
    
}
