package com.im.lac.job.step;

import com.im.lac.job.jobdef.*;

/**
 * Interface for a job that writes a dataset . The following options are
 * possible:
 * <ol>
 * <li>Update the dataset with the results (datasetId will be defined, mode will
 * be UPDATE, datasetName will be ignored)</li>
 * <li>Create a new dataset with the results (datasetId will be defined, mode
 * will be CREATE, datasetName will be specified)</li>
 * </ol>
 *
 * @author timbo
 */
public class WriteDatasetStepDefinition extends ConsumingStepDefinition {

    private Long datasetId;
    private DatasetMode datasetMode;
    private String datasetName;

    public WriteDatasetStepDefinition() {
    }

    public WriteDatasetStepDefinition(Long datasetId, DatasetMode datasetMode, String datasetName) {
        configureWriteDataset(datasetId, datasetMode, datasetName);
    }

    public enum DatasetMode {

        UPDATE, CREATE
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public DatasetMode getDatasetMode() {
        return datasetMode;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public final void configureWriteDataset(Long datasetId, DatasetMode datasetMode, String datasetName) {
        this.datasetId = datasetId;
        this.datasetMode = datasetMode;
        this.datasetName = datasetName;
    }

}
