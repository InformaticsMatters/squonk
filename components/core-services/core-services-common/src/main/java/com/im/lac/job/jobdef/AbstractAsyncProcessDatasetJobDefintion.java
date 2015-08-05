package com.im.lac.job.jobdef;

import com.im.lac.services.AccessMode;
import com.im.lac.services.ServiceDescriptor;
import java.util.Map;

/**
 *
 * @author timbo
 */
public abstract class AbstractAsyncProcessDatasetJobDefintion
        implements ServiceExecutionJobDefinition, DatasetJobDefinition {

    private String serviceId;
    private String accessModeId;
    private Map<String, Object> parameters;

    private Long datasetId;
    private DatasetMode datasetMode;
    private String datasetName;

    public AbstractAsyncProcessDatasetJobDefintion() {}
    
    public AbstractAsyncProcessDatasetJobDefintion(
            String serviceId,
            String accessModeId,
            Map<String, Object> parameters,
            Long datasetId,
            DatasetMode datasetMode,
            String datasetName) {
        this.serviceId = serviceId;
        this.accessModeId = accessModeId;
        this.parameters = parameters;
        this.datasetId = datasetId;
        this.datasetMode = datasetMode;
        this.datasetName = datasetName;
    }

    @Override
    public void configureService(ServiceDescriptor serviceDescriptor, AccessMode accessMode, Map<String, Object> parameters) {
        this.serviceId = serviceDescriptor.getId();
        this.accessModeId = accessMode.getId();
        this.parameters = parameters;
    }

    //@Override
    public void configureDataset(Long datasetId, DatasetMode datasetMode, String newDatasetName) {
        this.datasetId = datasetId;
        this.datasetMode = datasetMode;
        this.datasetName = newDatasetName;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getAccessModeId() {
        return accessModeId;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public Long getDatasetId() {
        return datasetId;
    }

    @Override
    public DatasetMode getMode() {
        return datasetMode;
    }

    @Override
    public String getDatasetName() {
        return datasetName;
    }

}
