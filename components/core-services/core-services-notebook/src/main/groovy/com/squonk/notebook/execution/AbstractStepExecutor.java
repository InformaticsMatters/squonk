package com.squonk.notebook.execution;

import com.im.lac.job.jobdef.StepDefinition;
import com.squonk.notebook.api.CellDTO;
import com.squonk.notebook.api.CellType;
import com.squonk.notebook.client.CallbackClient;
import com.squonk.notebook.execution.steps.StepExecutor;
import com.squonk.notebook.execution.variable.CellCallbackClientVariableLoader;
import com.squonk.notebook.execution.variable.VariableManager;
import javax.inject.Inject;

/**
 *
 * @author timbo
 */
public abstract class AbstractStepExecutor implements QndCellExecutor {

    @Inject
    protected CallbackClient callbackClient;

    protected abstract String getCellTypeName();

    protected abstract StepDefinition[] getStepDefintions(CellDTO cell);

    @Override
    public boolean handles(CellType cellType) {
        return cellType.getName().equals(getCellTypeName());
    }

    @Override
    public void execute(String cellName) {

        VariableManager manger = new VariableManager(
                new CellCallbackClientVariableLoader(callbackClient, cellName));
        CellDTO cell = callbackClient.retrieveCell(cellName);

        StepDefinition[] steps = getStepDefintions(cell);

        StepExecutor executor = new StepExecutor(manger);
        try {
            executor.execute(steps, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute cell", e);
        }
    }

}
