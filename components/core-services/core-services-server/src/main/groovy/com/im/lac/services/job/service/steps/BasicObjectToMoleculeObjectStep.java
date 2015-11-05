package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import com.im.lac.types.BasicObject;
import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetProvider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.camel.CamelContext;

/**
 * Converts a Dataset&lt;BasicObject&gt; to a Dataset&lt;MoleculeObject&gt;
 * given a specified field name that contains the structure. A default name of
 * "structure" is used if option is not specified. The field name is handled in
 * a case insensitive manner.
 *
 * NOTE: if a field with that name is not found MoleculeObjects with an empty
 * structure is generated.
 *
 * NOTE: does not perform any validation of the structure - if bad structures
 * are present errors will occur later when these are parsed.
 *
 * NOTE: This step generates a new Stream that contains the functions necessary
 * to convert the BasicObjects to the MoleculeObjects and creates a new Dataset
 * with that new Stream, but it does NOT call a terminal operation on the Stream
 * to actually do the conversions. You are responsible for providing that
 * terminal operation on the output Dataset.
 *
 * @author timbo
 */
public class BasicObjectToMoleculeObjectStep extends AbstractStep {

    /** The name of the value in the BasicObject that contains the structure */
    public static final String OPTION_STRUCTURE_FIELD_NAME = "StructureFieldName";
    /** Optional value for the structure format. e.g. "smiles". */
    public static final String OPTION_STRUCTURE_FORMAT = "StructureFormat";
    /** Option for whether to use the UUID of BasicObject as the UUID of the new MolecuelObject.
     * Default is true. Expects a boolean value.
     */  
    public static final String OPTION_PRESERVE_UUID = "PreserveUuid";

    /** The field for the source Dataset&lt;BasicObject&gt; */
    public static final String FIELD_INPUT_DATASET = "_BasicObjectToMoleculeObjectInputDataset";
    /** The field for the resulting Dataset&lt;MoleculeObject&gt; */
    public static final String FIELD_OUTPUT_DATASET = "_BasicObjectToMoleculeObjectOutputDataset";

    public static String DEFAULT_STRUCTURE_FIELD_NAME = "structure";

    @Override
    public String[] getInputVariableNames() {
        return new String[]{FIELD_INPUT_DATASET};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{FIELD_OUTPUT_DATASET};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        String structureFieldName = getOption(OPTION_STRUCTURE_FIELD_NAME, String.class, DEFAULT_STRUCTURE_FIELD_NAME);
        String structureFormat = getOption(OPTION_STRUCTURE_FORMAT, String.class);
        boolean preserveUuid = getOption(OPTION_PRESERVE_UUID, Boolean.class, true);

        DatasetProvider p = fetchMappedValue(FIELD_INPUT_DATASET, DatasetProvider.class, varman);
        Dataset ds1 = p.getDataset();
        Stream<BasicObject> stream1 = ds1.getStream();
        Stream<MoleculeObject> stream2 = stream1.map((bo) -> {
            return convert(bo, structureFieldName, structureFormat, preserveUuid);
        });

        Dataset<MoleculeObject> mods = new Dataset<>(MoleculeObject.class, stream2);

        createMappedVariable(FIELD_OUTPUT_DATASET, Dataset.class, mods, Variable.PersistenceType.DATASET, varman);
    }

    private MoleculeObject convert(BasicObject bo, String structureFieldName, String format, boolean preserveUuid) {

        Map<String, Object> vals = new LinkedHashMap<>();
        String struct = null;
        for (Map.Entry<String, Object> e : bo.getValues().entrySet()) {
            if (structureFieldName.equalsIgnoreCase(e.getKey())) {
                Object o = e.getValue();
                if (o != null) {
                    struct = o.toString();
                }
            } else {
                vals.put(e.getKey(), e.getValue());
            }
        }
        return new MoleculeObject(preserveUuid ? bo.getUUID() : null, struct, format, vals);
    }

}
