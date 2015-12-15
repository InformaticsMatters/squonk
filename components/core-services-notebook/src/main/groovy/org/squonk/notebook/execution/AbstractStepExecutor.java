package org.squonk.notebook.execution;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.api.OptionDTO;
import org.squonk.notebook.client.CallbackClient;
import org.squonk.execution.steps.StepExecutor;
import org.squonk.execution.variable.impl.CellCallbackClientVariableLoader;
import org.squonk.execution.variable.VariableManager;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author timbo
 */
public abstract class AbstractStepExecutor implements QndCellExecutor {

    private static final Logger LOG = Logger.getLogger(AbstractStepExecutor.class.getName());

    @Inject
    protected CallbackClient callbackClient;

    protected final String cellTypeName;


    public AbstractStepExecutor(String cellTypeName) {
        this.cellTypeName = cellTypeName;
    }

    protected abstract StepDefinition[] getStepDefintions(CellDTO cell);

    @Override
    public boolean handles(CellType cellType) {
        LOG.info("Testing cell type name " + cellType.getName() + " for " + cellTypeName);
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
        OptionDTO dto = cell.getOptionMap().get(option);
        if (dto == null) {
            if (LOG.isLoggable(Level.INFO)) {
                String values = cell.getOptionMap().keySet().stream().collect(Collectors.joining(","));
                LOG.info("Option " + option + " not found. Possible values are " + values);
            }
            return step;
        } else {
            return step.withOption(option, dto.getValue());
        }
    }

}
