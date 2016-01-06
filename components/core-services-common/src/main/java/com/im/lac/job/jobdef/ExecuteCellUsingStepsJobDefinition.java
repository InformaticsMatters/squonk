package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.execution.steps.StepDefinition;

/**
 * Created by timbo on 31/12/15.
 */
public class ExecuteCellUsingStepsJobDefinition implements StepsCellExecutorJobDefinition {

    private Long notebookId;
    private String cellName;
    private StepDefinition[] steps;

    public ExecuteCellUsingStepsJobDefinition() {}

    public ExecuteCellUsingStepsJobDefinition(
            @JsonProperty("notebookId") Long notebookId,
            @JsonProperty("cellName") String cellName,
            @JsonProperty("steps") StepDefinition[] steps) {
        this.notebookId = notebookId;
        this.cellName = cellName;
        this.steps = steps;
    }

    public ExecuteCellUsingStepsJobDefinition(
            Long notebookId,
            String cellName,
           StepDefinition step) {
        this.notebookId = notebookId;
        this.cellName = cellName;
        this.steps = new StepDefinition[] { step };
    }

    public Long getNotebookId() {
        return notebookId;
    }

    public String getCellName() {
        return cellName;
    }

    public StepDefinition[] getSteps() {
        return steps;
    }

    public void configureCellAndSteps(Long notebookId, String cellName, StepDefinition[] steps) {
        this.notebookId = notebookId;
        this.cellName = cellName;
        this.steps = steps;
    }

}
