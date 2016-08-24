package org.squonk.camel.rdkit.processor;

import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.rdkit.mol.EvaluatorDefinition;
import org.squonk.rdkit.mol.MolEvaluator;
import org.squonk.rdkit.mol.MolReader;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.ExecutionStats;
import org.squonk.util.Metrics;
import org.squonk.util.StatsRecorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class RDKitMoleculeProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(RDKitMoleculeProcessor.class.getName());

    List<EvaluatorDefinition> definitions = new ArrayList<>();

    @Override
    public void process(Exchange exch) throws Exception {
        Dataset<MoleculeObject> dataset = exch.getIn().getBody(Dataset.class);
        if (dataset == null || dataset.getType() != MoleculeObject.class) {
            throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
        }
        List<EvaluatorDefinition> defs = definitions;
        Stream<MoleculeObject> mols = dataset.getStream();

        Map<String,Integer> stats = new HashMap<>();
        Stream<MoleculeObject> results = evaluate(exch, mols, defs, stats);

        StatsRecorder recorder = exch.getIn().getHeader(StatsRecorder.HEADER_STATS_RECORDER, StatsRecorder.class);
        if (recorder != null) {
            results = results.onClose(() -> {
                recorder.recordStats(stats);
            });
        }

        DatasetMetadata meta = handleMetadata(exch, dataset.getMetadata(), defs);
        LOG.info("Generated metadata: " + JsonHandler.getInstance().objectToJson(meta));
        exch.getIn().setBody(new MoleculeObjectDataset(results, meta));
    }

    protected DatasetMetadata handleMetadata(Exchange exch, DatasetMetadata original, List<EvaluatorDefinition> definitions) throws IllegalAccessException, InstantiationException {
        if (original == null) {
            original = new DatasetMetadata(MoleculeObject.class);
        }
        String now = DatasetMetadata.formatDate();
        //String version = "RDKit version " + RDKFuncs.getRdkitVersion();
        String version = "RDKit version xyz";
        System.out.println(version);
        for (EvaluatorDefinition eval : definitions) {
            String name = eval.propName;
            Class type = eval.function.getType();
            original.getValueClassMappings().put(name, type);
            original.putFieldMetaProp(name, DatasetMetadata.PROP_CREATED, now);
            original.putFieldMetaProp(name, DatasetMetadata.PROP_SOURCE, version);
            original.putFieldMetaProp(name, DatasetMetadata.PROP_DESCRIPTION, "Molecular property calculation: " + eval.function.toString());
            original.appendDatasetHistory("Added field " + name);
        }
        return original;
    }

    Stream<MoleculeObject> evaluate(Exchange exchange, Stream<MoleculeObject> mols, List<EvaluatorDefinition> definitions, Map<String,Integer> stats) {

        return mols.peek((mo) -> {
            ROMol rdkitMol = MolReader.findROMol(mo);
            if (rdkitMol != null) {
                definitions.stream().forEach((definition) -> {
                    MolEvaluator.evaluate(mo, rdkitMol, definition);
                    ExecutionStats.increment(stats, Metrics.generate(Metrics.PROVIDER_RDKIT, definition.function.getMetricsCode()), 1);
                });
            } else {
                LOG.warning("No molecule found to process");
            }
        });
    }


    /**
     * Add a new calculation using an expression.
     *
     * TODO allow the expressions to be read from header to allow dynamic configuration.
     * <br>
     * Note: the return type is the instance, to allow the fluent builder pattern to be used.
     *
     * @param name The name for the calculated property
     * @param function The RDKit function to execute
     * @return
     */
    public RDKitMoleculeProcessor calculate(EvaluatorDefinition.Function function, String name) {
        definitions.add(EvaluatorDefinition.calculate(function, name));
        return this;
    }

    public RDKitMoleculeProcessor calculate(EvaluatorDefinition.Function function) {
        definitions.add(EvaluatorDefinition.calculate(function, function.getName()));
        return this;
    }

}
