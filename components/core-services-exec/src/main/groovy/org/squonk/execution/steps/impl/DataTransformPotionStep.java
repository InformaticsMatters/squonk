package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.camel.processor.ValueTransformerProcessor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.transform.AbstractTransform;
import org.squonk.dataset.transform.Message;
import org.squonk.dataset.transform.PotionParser;
import org.squonk.dataset.transform.TransformDefinitions;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author timbo
 */
public class DataTransformPotionStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(DataTransformPotionStep.class.getName());

    public static final String OPTION_POTION = StepDefinitionConstants.DataTransformPotion.OPTION_POTION;

    /**
     * Add the transforms to the dataset Stream. NOTE: transforms will not occur
     * until a terminal operation is performed on the Stream. Normally no output is
     * created as the transforms are added to the input dataset which will be
     * transient, however if an output field is needed then specify a mapping for the 
     * field named FIELD_OUTPUT_DATASET. 
     *
     * @param varman
     * @param context
     * @throws Exception
     */
    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        statusMessage = MSG_PREPARING_INPUT;
        Dataset ds = fetchMappedInput("input", Dataset.class, varman);
        if (ds == null) {
            throw new IllegalStateException("Input variable not found: input");
        }

        String potion = getOption(OPTION_POTION, String.class);
        if (potion == null) {
            throw new IllegalStateException("Potion must be defined as option named " + OPTION_POTION);
        }

        PotionParser parser = TransformDefinitions.parse(potion, ds.getMetadata().getValueClassMappings());
        if (parser.getErrorCount() > 0) {
            statusMessage = parser.getMessages().stream()
                    .filter(m -> m.getLevel() == Message.Severity.Error)
                    .map(m -> m.toString())
                    .collect(Collectors.joining("\n"));
            // TODO - how best to notify user?
            return;
        }

        List<AbstractTransform> list = parser.getTransforms();
        if (list.isEmpty()) {
            statusMessage = "No transform definitions found";
            return;
        }
        TransformDefinitions txs = new TransformDefinitions(list);
        LOG.info("Transform Definitions: " + txs);
        statusMessage = "Transforming dataset ...";
        ValueTransformerProcessor p = ValueTransformerProcessor.create(txs);
        Dataset result = p.execute(context.getTypeConverter(), ds);

        LOG.info("Transforms defined");

        createMappedOutput("output", Dataset.class, result, varman);

        statusMessage = String.format(MSG_RECORDS_PROCESSED, result.getMetadata().getSize());
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(result.getMetadata()));
    }

}
