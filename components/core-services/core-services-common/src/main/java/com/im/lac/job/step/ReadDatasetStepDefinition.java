package com.im.lac.job.step;

/**
 * Interface for a job that reads a dataset and sends it to the next step for
 * processing.
 *
 *
 * @author timbo
 */
public class ReadDatasetStepDefinition extends StepDefinition {

    public ReadDatasetStepDefinition() {
    }

    public ReadDatasetStepDefinition(Long datasetId) {
        configureReadDataset(datasetId);
    }

    private Long datasetId;

    public Long getDatasetId() {
        return datasetId;
    }

    public final void configureReadDataset(Long datasetId) {
        this.datasetId = datasetId;
    }

}
