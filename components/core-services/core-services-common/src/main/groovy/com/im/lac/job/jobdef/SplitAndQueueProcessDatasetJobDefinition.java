package com.im.lac.job.jobdef;

/**
 *
 * @author timbo
 */
public class SplitAndQueueProcessDatasetJobDefinition extends ProcessDatasetJobDefinition {

    public SplitAndQueueProcessDatasetJobDefinition(
            Long datasetId,
            String destination,
            DatasetMode mode,
            Class resultType,
            String datasetName) {
        super(datasetId, destination, mode, resultType, datasetName);
    }

    public SplitAndQueueProcessDatasetJobDefinition(
            Long datasetId,
            String destination,
            DatasetMode mode,
            Class resultType) {
        super(datasetId, destination, mode, resultType);
    }
    
}
