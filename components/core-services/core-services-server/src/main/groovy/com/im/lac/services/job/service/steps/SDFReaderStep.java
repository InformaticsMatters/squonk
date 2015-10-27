package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.Dataset;
import com.squonk.reader.SDFReader;
import com.squonk.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import org.apache.camel.CamelContext;

/**
 * Reads a SDFile object and generates a MoleculeObjectDataset. The SDFile holds
 * an InputStream with the contents of SD file (can be gzipped). By default the
 * input SDFile object is expected in the variable named by the FIELD_SDF_INPUT
 * constant, though that name can be mapped to a different name. The resulting
 * MoleculeObjectDataset is contained in the variable named by the
 * FIELD_DATASET_OUTPUT constant.
 *
 * @author timbo
 */
public class SDFReaderStep extends AbstractStep {

    /**
     * How to handle the name field (the first line of the CTAB block). See
     * {@link com.squonk.reader.SDFReader} for details.
     */
    public static final String OPTION_NAME_FIELD_NAME = "NameFieldName";
    /**
     * Expected variable name for the input
     */
    public static final String FIELD_SDF_INPUT = "SDFInput";
    /**
     * Variable name for the MoleculeObjectDataset output
     */
    public static final String FIELD_DATASET_OUTPUT = "SDFOutput";

    @Override
    public void execute(VariableManager varman, CamelContext context) throws IOException {
        InputStream is = fetchMappedValue(FIELD_SDF_INPUT, InputStream.class, varman);
        SDFReader reader = createReader(IOUtils.getGunzippedInputStream(is));
        Stream<MoleculeObject> mols = reader.asStream();
        Dataset dataset = new Dataset(MoleculeObject.class, mols);
        varman.createVariable(FIELD_DATASET_OUTPUT, Dataset.class, dataset, Variable.PersistenceType.NONE);
    }

    private SDFReader createReader(InputStream input) throws IOException {
        SDFReader reader = new SDFReader(input);
        String nameFieldName = getOption(OPTION_NAME_FIELD_NAME, String.class);
        reader.setNameFieldName(nameFieldName);
        return reader;
    }

}
