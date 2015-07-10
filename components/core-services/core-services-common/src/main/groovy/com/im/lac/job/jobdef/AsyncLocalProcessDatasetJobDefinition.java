package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
public class AsyncLocalProcessDatasetJobDefinition extends AbstractProcessDatasetJobDefinition {

    private final String endpoint;

    @JsonCreator
    public AsyncLocalProcessDatasetJobDefinition(
            @JsonProperty("datasetId") Long datasetId,
            @JsonProperty("endpoint") String endpoint,
            @JsonProperty("mode") DatasetMode mode,
            @JsonProperty("resultType") Class resultType,
            @JsonProperty("datasetName") String datasetName) {
        super(datasetId, mode, resultType, datasetName);
        this.endpoint = endpoint;
    }

    public AsyncLocalProcessDatasetJobDefinition(
            Long datasetId,
            String endpoint,
            DatasetMode mode,
            Class resultType) {
        super(datasetId, mode, resultType);
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

}
