package com.im.lac.job.jobdef;

/**
 *
 * @author timbo
 */
public interface DatasetJobDefinition extends JobDefinition {

    public enum DatasetMode {

        UPDATE, CREATE
    }

    Long getDatasetId();

    DatasetMode getMode();

    String getDatasetName();
    
    //void configureDataset(Long datasetId, DatasetMode datasetMode, String newDatasetName);

}
