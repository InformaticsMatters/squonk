package com.squonk.notebook.execution;

import com.squonk.execution.steps.StepDefinition;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.client.CallbackClient;
import com.squonk.execution.steps.StepExecutor;
import com.squonk.execution.variable.impl.CellCallbackClientVariableLoader;
import com.squonk.execution.variable.VariableManager;
import javax.inject.Inject;

/**
 *
 * @author timbo
 */
public abstract class AbstractStepExecutor implements QndCellExecutor {

    @Inject
    protected CallbackClient callbackClient;
    
    protected final String cellTypeName;
    
    
    public AbstractStepExecutor(String cellTypeName) {
        this.cellTypeName = cellTypeName;
    }

    protected abstract StepDefinition[] getStepDefintions(CellDTO cell);

    @Override
    public boolean handles(CellType cellType) {
        System.out.println("Testing cell type name " + cellType.getName() + " for " + cellTypeName);
        return cellType.getName().equals(cellTypeName);
    }

    @Override
    public void execute(String cellName) {
        VariableManager manger = new VariableManager(new CellCallbackClientVariableLoader(callbackClient, cellName));
        CellDTO cell = callbackClient.retrieveCell(cellName);

        StepDefinition[] steps = getStepDefintions(cell);

        StepExecutor executor = new StepExecutor(manger);
        try {
            executor.execute(steps, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute cell", e);
        }
    }

    protected StepDefinition configureOption(StepDefinition step, CellDTO cell, String option) {
        return step.withOption(option, cell.getPropertyMap().get(option));
    }

}
