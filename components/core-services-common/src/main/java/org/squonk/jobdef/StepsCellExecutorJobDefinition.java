package org.squonk.jobdef;

import org.squonk.execution.steps.StepDefinition;

/**
 * Created by timbo on 31/12/15.
 */
public interface StepsCellExecutorJobDefinition extends CellExecutorJobDefinition {

    StepDefinition[] getSteps();
    void configureCellAndSteps(Long notebookId, Long editableId, Long cellId, StepDefinition... steps);
}
