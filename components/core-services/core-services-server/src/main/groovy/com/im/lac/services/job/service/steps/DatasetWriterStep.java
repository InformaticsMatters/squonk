package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.VariableManager;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.dataset.DatasetProvider;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class DatasetWriterStep extends AbstractStep {
    
    public static final String OPTION_SOURCE_DATASET = "SourceDataset";
    public static final String OPTION_OUTPUT_DATA = "OutputData";
    public static final String OPTION_OUTPUT_METADATA = "OutputMetadata";

    @Override
    public void execute(VariableManager varman) throws Exception {
        DatasetProvider p = fetchMappedValue(OPTION_SOURCE_DATASET, DatasetProvider.class, varman);
        Dataset ds = p.getDataset();
        Stream s = ds.createMetadataGeneratingStream(ds.getStream());
        ds.replaceStream(s);
        try (InputStream is = ds.getInputStream(true)) {
            varman.writeVariable(OPTION_OUTPUT_DATA, Dataset.class, is, true);
        }
        varman.createVariable(OPTION_OUTPUT_METADATA, DatasetMetadata.class, ds.getMetadata(), true);
    }
    
    
}
