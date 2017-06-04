package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;

import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class DatasetSelectSliceStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(DatasetSelectSliceStep.class.getName());

    public static final String OPTION_SKIP = StepDefinitionConstants.DatasetSelectSlice.OPTION_SKIP;
    public static final String OPTION_COUNT = StepDefinitionConstants.DatasetSelectSlice.OPTION_COUNT;

    /**
     * Create a slice of the dataset skipping a number of records specified by the skip option (or 0 if not specified)
     * and including only the number of records specified by the count option (or till teh end if not specified).
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
        LOG.fine("Input Dataset: " + ds);


        Integer skip = getOption(OPTION_SKIP, Integer.class);
        Integer count = getOption(OPTION_COUNT, Integer.class);

        statusMessage = "Setting filters ...";
        Stream stream = (Stream)ds.getStream().sequential();
        if (skip != null) {
            LOG.info("Setting skip to " + skip);
            stream = stream.skip(skip);
        }
        if (count != null) {
            LOG.info("Setting count to " + count);
            stream = stream.limit(count);
        }
        DatasetMetadata meta = ds.getMetadata();
        meta.setSize(0); // will be recalculated

        Dataset results = new Dataset(stream, meta);
        
        String outFldName = mapOutputVariable("output");
        if (outFldName != null) {
            createVariable(outFldName, Dataset.class, results, varman);
        }

        statusMessage = generateStatusMessage(ds.getSize(), results.getSize(), -1);
        LOG.info("Results: " + ds.getMetadata());
    }

}
