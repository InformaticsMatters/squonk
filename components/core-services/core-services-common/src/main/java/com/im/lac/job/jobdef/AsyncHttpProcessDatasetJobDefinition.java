package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class AsyncHttpProcessDatasetJobDefinition
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
    public AsyncHttpProcessDatasetJobDefinition(
            @JsonProperty("serviceId") String serviceId,
            @JsonProperty("accessModeId") String accessModeId,
            @JsonProperty("parameters") Map<String, Object> parameters,
            @JsonProperty("datasetId") Long datasetId,
            @JsonProperty("datasetMode") DatasetMode datasetMode,
            @JsonProperty("datasetName") String datasetName) {
        super(serviceId, accessModeId, parameters, datasetId, datasetMode, datasetName);
    }

    /**
     * Zero-arg constructor for use on client. The various configureXxx() methods are defined as
     * part of interface definitions according to the type of job. If its a job type that uses
     * services then its interface includes a configureService(...) method. If its a job type that
     * handles a dataset then its interface includes a configureDataset(...) method. The client
     * should know from that nature of the operation what interfaces are involved (e.g. dropping a
     * dataset onto a service uses both types of interface).
     *
     * The basic flow would be like this: 1. the ServiceDescriptor's AccessMode specifies the class
     * name of the JobDefintion and the necessary information to create the UI. 2. When user
     * executes the specified service the JobDefintion class is instantiated with this zero-arg
     * constructor. 3. If the JobDefintion implements the appropriate interfaces it is configured by
     * calling those methods accordingly. 4. The configured JobDefintion is sent for execution and
     * contains all the information from the client that is needed.
     */
    public AsyncHttpProcessDatasetJobDefinition() {
        super();
    }

}
