package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.dataset.DatasetProvider;
import com.squonk.types.io.JsonHandler;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class DatasetReaderStep extends AbstractStep {

    public static final String FIELD_OUTPUT_DATASET = "OutputDataset";
    public static final String FIELD_INPUT_DATA = "InputData";
    public static final String FIELD_INPUT_METADATA = "InputMetadata";

    @Override
    public void execute(VariableManager varman) throws Exception {
        String d = fetchMappedValue(FIELD_INPUT_DATA, String.class, varman);
        String m = fetchMappedValue(FIELD_INPUT_METADATA, String.class, varman);
        DatasetMetadata md = JsonHandler.getInstance().objectFromJson(m, DatasetMetadata.class);
        Dataset ds = JsonHandler.getInstance().unmarshalDataset(md, d);
        varman.createVariable(FIELD_OUTPUT_DATASET, Dataset.class, ds, true);
    }

}
