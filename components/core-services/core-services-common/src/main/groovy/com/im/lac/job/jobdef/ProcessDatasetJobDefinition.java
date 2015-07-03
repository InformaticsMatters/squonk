package com.im.lac.job.jobdef;


/**
 *
 * @author timbo
 */
public abstract class ProcessDatasetJobDefinition implements DatasetJobDefinition {

    private final Long datasetId;

    private final String destination;

    private final DatasetMode mode;
    
    private final Class resultType;
    
    private final String datasetName;

    public ProcessDatasetJobDefinition(
            Long datasetId, 
            String destination, 
            DatasetMode mode, 
            Class resultType, 
            String datasetName) {
        this.datasetId = datasetId;
        this.destination = destination;
        this.mode = mode;
        this.resultType = resultType;
        this.datasetName = datasetName;
    }
    
    public ProcessDatasetJobDefinition(
            Long datasetId, 
            String destination, 
            DatasetMode mode, 
            Class resultType) {
        this.datasetId = datasetId;
        this.destination = destination;
        this.mode = mode;
        this.resultType = resultType;
        this.datasetName = null;
    }

    @Override
    public Long getDatasetId() {
        return datasetId;
    }

    @Override
    public String getDestination() {
        return destination;
    }

    @Override
    public DatasetMode getMode() {
        return mode;
    }

    public Class getResultType() {
        return resultType;
    }

    @Override
    public String getDatasetName() {
        return datasetName;
    }

}
