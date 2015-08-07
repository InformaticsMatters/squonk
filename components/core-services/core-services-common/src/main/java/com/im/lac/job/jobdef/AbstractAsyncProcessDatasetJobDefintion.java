package com.im.lac.job.jobdef;

import com.im.lac.services.AccessMode;
import com.im.lac.services.ServiceDescriptor;
import java.util.Map;

/**
 * Base class for jobs that process a dataset using a service. Concrete implementations extend this
 * class. Typical usage:
 * <ol>
 * <li>Start with a {@link ServiceDescriptor} and a chosen {@link AccessMode}. This defines the service endpoint.</li>
 * <li>The {@link AccessMode.getJobType()} method defines the type of JobDefinition that is needed</li>
 * <li>Create a new instance of the JobDefinition class using the zero arg constructor.</li>
 * <li>Based of your context there will be a limited number of interfaces that the JobDefinition
 * might implement. In this case it will implement {@link ServiceExecutionJobDefinition} and {@link ProcessDatasetJobDefinition}</li>
 * <li>For each of these possible interfaces check whether the JobDefintion implements the appropriate interface and if so 
 * call the corresponding method in the interface. e.g. if it implements ServiceExecutionJobDefinition then call
 * {@link ServiceExecutionJobDefinition.configureService(ServiceDescriptor serviceDescriptor, AccessMode accessMode, Map<String,Object>
 * parameters)</li>
 * <li>Submit the job definition using the {@link JobClient}.</li>
 * </ol>
 * <br>
 * Potential issue: we assume we always start with a ServiceDescriptor. There could be jobs that don't use services?
 *
 * @author timbo
 */
public abstract class AbstractAsyncProcessDatasetJobDefintion
        implements ServiceExecutionJobDefinition, ProcessDatasetJobDefinition {

    private String serviceId;
    private String accessModeId;
    private Map<String, Object> parameters;

    private Long datasetId;
    private DatasetMode datasetMode;
    private String datasetName;

    public AbstractAsyncProcessDatasetJobDefintion() {
    }

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
    public DatasetMode getDatasetMode() {
        return datasetMode;
    }

    @Override
    public String getDatasetName() {
        return datasetName;
    }

}
