package org.squonk.notebook.execution;

import org.apache.camel.impl.DefaultCamelContext;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.notebook.api.BindingDTO;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.api.OptionDTO;
import org.squonk.notebook.client.CallbackClient;
import org.squonk.execution.steps.StepExecutor;
import org.squonk.execution.variable.impl.CellCallbackClientVariableLoader;
import org.squonk.execution.variable.VariableManager;

import javax.inject.Inject;
import java.util.Map;
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

        CellDTO cell = callbackClient.retrieveCell(cellName);
        if (cell == null) {
            throw new IllegalStateException("Cell named " + cellName + " not found in notebook");
        }
        VariableManager manager = new VariableManager(new CellCallbackClientVariableLoader(callbackClient));

        StepDefinition[] steps = getStepDefintions(cell);

        StepExecutor executor = new StepExecutor(cellName, manager);
        try {
            // TODO - get the real camel context
            executor.execute(steps, new DefaultCamelContext());
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute cell", e);
        }
    }

    protected StepDefinition configureOption(StepDefinition step, CellDTO cell, String option) {
    return configureOption(step, cell, option, false);
    }

    protected StepDefinition configureOption(StepDefinition step, CellDTO cell, String option, boolean required) {
        OptionDTO dto = cell.getOptionMap().get(option);
        if (dto == null || dto.getValue() == null) {
            if (required) {
                String values = cell.getOptionMap().keySet().stream().collect(Collectors.joining(","));
                throw new IllegalStateException("Option " + option + " not defined. Possible values are: " + values);
            } else {
                return step;
            }
        } else {
            return step.withOption(option, dto.getValue());
        }
    }

    protected void dumpCellConfig(CellDTO cell, Level level) {
        for (Map.Entry<String,BindingDTO> e : cell.getBindingMap().entrySet()) {
            LOG.log(level, "BINDING: " + e.getKey() + " -> " + e.getValue());
        }

        for (Map.Entry<String,OptionDTO> e : cell.getOptionMap().entrySet()) {
            LOG.log(level, "OPTION: " + e.getKey() + " -> " + e.getValue());
        }
    }

}
