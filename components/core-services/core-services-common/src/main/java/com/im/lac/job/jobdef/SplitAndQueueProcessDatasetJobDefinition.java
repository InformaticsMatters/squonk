package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
public class SplitAndQueueProcessDatasetJobDefinition extends AbstractProcessDatasetJobDefinition {

    private final String queuename;

    public SplitAndQueueProcessDatasetJobDefinition(
            @JsonProperty("datasetId") Long datasetId,
            @JsonProperty("queuename") String queuename,
            @JsonProperty("datasetMode") DatasetMode datasetMode,
            @JsonProperty("resultType") Class resultType,
            @JsonProperty("datasetName") String datasetName) {
        super(datasetId, datasetMode, resultType, datasetName);
        this.queuename = queuename;
    }

    public SplitAndQueueProcessDatasetJobDefinition(
            Long datasetId,
            String queuename,
            DatasetMode mode,
            Class resultType) {
        super(datasetId, mode, resultType);
        this.queuename = queuename;
    }

    public String getQueuename() {
        return queuename;
    }

    @Override
    public void configureDataset(Long datasetId, DatasetMode datasetMode, String newDatasetName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
