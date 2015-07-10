package com.im.lac.job.jobdef;


/**
 *
 * @author timbo
 */
public abstract class AbstractProcessDatasetJobDefinition implements DatasetJobDefinition {

    private final Long datasetId;


    private final DatasetMode mode;
    
    private final Class resultType;
    
    private final String datasetName;

    public AbstractProcessDatasetJobDefinition(
            Long datasetId, 
            DatasetMode mode, 
            Class resultType, 
            String datasetName) {
        this.datasetId = datasetId;
        this.mode = mode;
        this.resultType = resultType;
        this.datasetName = datasetName;
    }
    
    public AbstractProcessDatasetJobDefinition(
            Long datasetId, 
            DatasetMode mode, 
            Class resultType) {
        this.datasetId = datasetId;
        this.mode = mode;
        this.resultType = resultType;
        this.datasetName = null;
    }

    @Override
    public Long getDatasetId() {
        return datasetId;
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
