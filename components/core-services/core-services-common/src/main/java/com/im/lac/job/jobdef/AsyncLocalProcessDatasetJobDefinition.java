package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class AsyncLocalProcessDatasetJobDefinition
        extends AbstractAsyncProcessDatasetJobDefintion {

    /**
     * Constructor for JSON deserialization
     *
     * @param serviceId
     * @param accessModeId
     * @param parameters
     * @param datasetId
     * @param datasetMode
     * @param datasetName
     */
    @JsonCreator
    public AsyncLocalProcessDatasetJobDefinition(
            @JsonProperty("serviceId") String serviceId,
            @JsonProperty("accessModeId") String accessModeId,
            @JsonProperty("parameters") Map<String, Object> parameters,
            @JsonProperty("datasetId") Long datasetId,
            @JsonProperty("datasetMode") DatasetMode datasetMode,
            @JsonProperty("datasetName") String datasetName) {
        super(serviceId, accessModeId, parameters, datasetId, datasetMode, datasetName);
    }

    public AsyncLocalProcessDatasetJobDefinition() {
        super();
    }

}
