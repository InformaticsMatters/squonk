package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.service.adapters.HttpGenericParamsJobAdapter;
import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.apache.camel.CamelContext;

/** NOTE: this is work in progress
 *
 * @author timbo
 */
public class MoleculeServiceThinExecutorStep extends AbstractStep {

    public static final String OPTION_SERVICE_ENDPOINT = "ServiceEndpoint";
    public static final String OPTION_EXECUTION_PARAMS = "ExecutionParams";

    public static final String FIELD_INPUT_DATASET = "_MoleculeServiceThinExecutorInputDataset";
    public static final String FIELD_OUTPUT_DATASET = "_MoleculeServiceThinExecutorOutputDataset";

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

        Dataset<MoleculeObject> dataset = fetchMappedValue(FIELD_INPUT_DATASET, Dataset.class, varman);
        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);
        Map<String, Object> params = getOption(OPTION_EXECUTION_PARAMS, Map.class);

        Map<UUID, MoleculeObject> cache = new ConcurrentHashMap<>();
        Stream<MoleculeObject> stream = dataset.getStream()
                .map(m -> {
                    cache.put(m.getUUID(), m);
                    return new MoleculeObject(m.getUUID(), m.getSource(), m.getFormat());
                });

        InputStream isin = JsonHandler.getInstance().marshalStreamToJsonArray(stream, true);
        
        // send for execution
        HttpGenericParamsJobAdapter adapter = new HttpGenericParamsJobAdapter();
        InputStream isout = adapter.submit(context, endpoint, isin, params);

        // handle results
        Dataset<MoleculeObject> ds = JsonHandler.getInstance()
                .unmarshalDataset(new DatasetMetadata<MoleculeObject>(MoleculeObject.class), isout);

        ds.getStream().forEachOrdered(m -> {
            MoleculeObject o = cache.get(m.getUUID());
            if (o == null) {
                System.out.println("Molecule " + m.getUUID() + " not found. Strange!");
            } else {
                System.out.println("Found Mol " + m.getUUID());
                o.getValues().putAll(m.getValues());
            }
        });

        Dataset<MoleculeObject> results = new Dataset<>(MoleculeObject.class, cache.values());

        createMappedVariable(FIELD_OUTPUT_DATASET, Dataset.class, results, Variable.PersistenceType.DATASET, varman);
    }

}
