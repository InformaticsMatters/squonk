package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.execution.steps.StepDefinition;

/**
 * Created by timbo on 31/12/15.
 */
public class ExecuteCellUsingStepsJobDefinition implements StepsCellExecutorJobDefinition {

    private Long notebookId;
    private Long editableId;
    private Long cellId;
    private StepDefinition[] steps;

    public ExecuteCellUsingStepsJobDefinition() {}

    public ExecuteCellUsingStepsJobDefinition(
            @JsonProperty("notebookId") Long notebookId,
            @JsonProperty("editableId") Long editableId,
            @JsonProperty("cellId") Long cellId,
            @JsonProperty("steps") StepDefinition[] steps) {
        this.notebookId = notebookId;
        this.editableId = editableId;
        this.cellId = cellId;
        this.steps = steps;
    }

    public ExecuteCellUsingStepsJobDefinition(
            Long notebookId,
            Long editableId,
            Long cellId,
           StepDefinition step) {
        this.notebookId = notebookId;
        this.editableId = editableId;
        this.cellId = cellId;
        this.steps = new StepDefinition[] { step };
    }

    public Long getNotebookId() {
        return notebookId;
    }

    public Long getEditableId() {
        return editableId;
    }

    public Long getCellId() {
        return cellId;
    }

    public StepDefinition[] getSteps() {
        return steps;
    }

    public void configureCellAndSteps(Long notebookId, Long editableId, Long cellId, StepDefinition... steps) {
        this.notebookId = notebookId;
        this.editableId = editableId;
        this.cellId = cellId;
        this.steps = steps;
    }

}
