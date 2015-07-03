package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
public class AsyncProcessDatasetJobDefinition extends ProcessDatasetJobDefinition {

    @JsonCreator
    public AsyncProcessDatasetJobDefinition(
            @JsonProperty("datasetId") Long datasetId,
            @JsonProperty("destination") String destination,
            @JsonProperty("mode") DatasetMode mode,
            @JsonProperty("resultType") Class resultType,
            @JsonProperty("datasetName") String datasetName) {
        super(datasetId, destination, mode, resultType, datasetName);
    }

    
    public AsyncProcessDatasetJobDefinition(
            Long datasetId,
            String destination,
            DatasetMode mode,
            Class resultType) {
        super(datasetId, destination, mode, resultType);
    }
}
