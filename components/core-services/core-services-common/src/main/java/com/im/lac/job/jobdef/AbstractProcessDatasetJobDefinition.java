package com.im.lac.job.jobdef;


/**
 *
 * @author timbo
 */
public abstract class AbstractProcessDatasetJobDefinition implements ProcessDatasetJobDefinition {

    private final Long datasetId;


    private final DatasetMode datasetMode;
    
    private final Class resultType;
    
    private final String datasetName;

    public AbstractProcessDatasetJobDefinition(
            Long datasetId, 
            DatasetMode datasetMode, 
            Class resultType, 
            String datasetName) {
        this.datasetId = datasetId;
        this.datasetMode = datasetMode;
        this.resultType = resultType;
        this.datasetName = datasetName;
    }
    
    public AbstractProcessDatasetJobDefinition(
            Long datasetId, 
            DatasetMode datasetMode, 
            Class resultType) {
        this.datasetId = datasetId;
        this.datasetMode = datasetMode;
        this.resultType = resultType;
        this.datasetName = null;
    }

    @Override
    public Long getDatasetId() {
        return datasetId;
    }

    @Override
    public DatasetMode getDatasetMode() {
        return datasetMode;
    }

    public Class getResultType() {
        return resultType;
    }

    @Override
    public String getDatasetName() {
        return datasetName;
    }

}
