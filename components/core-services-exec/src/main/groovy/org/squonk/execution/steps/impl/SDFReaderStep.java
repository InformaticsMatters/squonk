package org.squonk.execution.steps.impl;

import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.Dataset;
import com.squonk.reader.SDFReader;
import com.squonk.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import org.apache.camel.CamelContext;

/**
 * Reads a SDFile and generates a {@link com.squonk.dataset.Dataset} of 
 * {@link com.im.lac.types.MoleculeObject}s. The SDFile is passed as an
 * {@link java.io.InputStream} (can be gzipped). By default the
 input is expected in the variable named by the VAR_SDF_INPUT
 constant, though that name can be mapped to a different name. The resulting
 Dataset is contained in the variable named by the VAR_DATASET_OUTPUT constant.
 *
 * @author timbo
 */
public class SDFReaderStep extends AbstractStep {

    /**
     * How to handle the name field (the first line of the CTAB block). See
     * {@link com.squonk.reader.SDFReader} for details.
     */
    public static final String OPTION_NAME_FIELD_NAME = "nameFieldName";
    /**
     * Expected variable name for the input
     */
    public static final String VAR_SDF_INPUT = "_SDFReaderSDFInput";
    /**
     * Variable name for the MoleculeObjectDataset output
     */
    public static final String VAR_DATASET_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public String[] getInputVariableNames() {
        return new String[]{VAR_SDF_INPUT};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{VAR_DATASET_OUTPUT};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws IOException {
        InputStream is = fetchMappedValue(VAR_SDF_INPUT, InputStream.class, PersistenceType.BYTES, varman);
        SDFReader reader = createReader(IOUtils.getGunzippedInputStream(is));
        Stream<MoleculeObject> mols = reader.asStream();
        Dataset dataset = new Dataset(MoleculeObject.class, mols);
        createMappedVariable(VAR_DATASET_OUTPUT, Dataset.class, dataset, PersistenceType.DATASET, varman);
    }

    private SDFReader createReader(InputStream input) throws IOException {
        SDFReader reader = new SDFReader(input);
        String nameFieldName = getOption(OPTION_NAME_FIELD_NAME, String.class);
        System.out.println("nameFieldName = " + nameFieldName);
        if (nameFieldName != null && nameFieldName.length() > 0) {
            reader.setNameFieldName(nameFieldName);
        } else {
            reader.setNameFieldName(null);
        }
        return reader;
    }

}
