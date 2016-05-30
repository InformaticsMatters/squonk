package org.squonk.camel.rdkit.processor;

import com.im.lac.types.MoleculeObject;
import org.RDKit.ROMol;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.rdkit.mol.EvaluatorDefintion;
import org.squonk.rdkit.mol.MolEvaluator;
import org.squonk.rdkit.mol.MolReader;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class RDKitMoleculeProcessor implements Processor {

    static {
        System.loadLibrary("GraphMolWrap");
    }

    private static final Logger LOG = Logger.getLogger(RDKitMoleculeProcessor.class.getName());

    List<EvaluatorDefintion> definitions = new ArrayList<>();

    @Override
    public void process(Exchange exchange) throws Exception {
        Dataset<MoleculeObject> dataset = exchange.getIn().getBody(Dataset.class);
        if (dataset == null || dataset.getType() != MoleculeObject.class) {
            throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
        }
        List<EvaluatorDefintion> defs = definitions;
        Stream<MoleculeObject> mols = dataset.getStream();

        Stream<MoleculeObject> results = evaluate(exchange, mols, defs);

        handleMetadata(exchange, dataset.getMetadata(), defs);
        exchange.getIn().setBody(new MoleculeObjectDataset(results));
    }

    protected void handleMetadata(Exchange exch, DatasetMetadata meta, List<EvaluatorDefintion> definitions) throws IllegalAccessException, InstantiationException {
        if (meta == null) {
            meta = new DatasetMetadata(MoleculeObject.class);
        }
        for (EvaluatorDefintion eval : definitions) {
            String name = eval.propName;
            Class type = eval.function.getType();
            meta.getValueClassMappings().put(name, type);
        }
        exch.getIn().setHeader(CamelCommonConstants.HEADER_METADATA, meta);
    }

    Stream<MoleculeObject> evaluate(Exchange exchange, Stream<MoleculeObject> mols, List<EvaluatorDefintion> definitions) {

        return mols.peek((mo) -> {
            ROMol rdkitMol = MolReader.findROMol(mo);
            if (rdkitMol != null) {
                definitions.stream().forEach((definition) -> {
                    MolEvaluator.evaluate(mo, rdkitMol, definition);
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
    public RDKitMoleculeProcessor calculate(EvaluatorDefintion.Function function, String name) {
        definitions.add(EvaluatorDefintion.calculate(function, name));
        return this;
    }

    public RDKitMoleculeProcessor calculate(EvaluatorDefintion.Function function) {
        definitions.add(EvaluatorDefintion.calculate(function, function.getName()));
        return this;
    }

}
