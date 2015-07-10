package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
public class AsyncHttpProcessDatasetJobDefinition extends AbstractProcessDatasetJobDefinition {

    private final String destination;

    @JsonCreator
    public AsyncHttpProcessDatasetJobDefinition(
            @JsonProperty("datasetId") Long datasetId,
            @JsonProperty("destination") String destination,
            @JsonProperty("mode") DatasetMode mode,
            @JsonProperty("resultType") Class resultType,
            @JsonProperty("datasetName") String datasetName) {
        super(datasetId, mode, resultType, datasetName);
        this.destination = destination;
    }

    public AsyncHttpProcessDatasetJobDefinition(
            Long datasetId,
            String destination,
            DatasetMode mode,
            Class resultType) {
        super(datasetId, mode, resultType);
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }
}
