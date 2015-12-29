package com.im.lac.camel.rdkit;

import com.im.lac.camel.processor.StreamingMoleculeObjectSourcer;
import com.im.lac.rdkit.mol.EvaluatorDefintion;
import com.im.lac.rdkit.mol.MolEvaluator;
import com.im.lac.rdkit.mol.MolReader;
import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.MoleculeObjectDataset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.RDKit.ROMol;

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
        StreamingMoleculeObjectSourcer sourcer = new StreamingMoleculeObjectSourcer() {
            @Override
            public void handleSingle(Exchange exchange, MoleculeObject mo) throws Exception {
                Stream<MoleculeObject> stream = evaluate(exchange, Stream.of(mo), definitions);
                MoleculeObject mol = stream.findFirst().orElse(null);
                exchange.getIn().setBody(mol);
            }

            @Override

            public void handleMultiple(Exchange exchange, Stream<MoleculeObject> mols) throws Exception {
                Stream<MoleculeObject> results = evaluate(exchange, mols, definitions);
                exchange.getIn().setBody(new MoleculeObjectDataset(results));
            }
        };
        sourcer.handle(exchange);
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
     * @param expression The chemical terms expression e.g. logP()
     * @return
     */
    public RDKitMoleculeProcessor calculate(String name, String expression) {
        definitions.add(EvaluatorDefintion.calculate(name, expression));
        return this;
    }
    
    public RDKitMoleculeProcessor calculate(String name, EvaluatorDefintion.Function function) {
        definitions.add(EvaluatorDefintion.calculate(name, function.toString()));
        return this;
    }

    /**
     * Create a new filter based on a chemical terms expression. The expression MUST evaluate to a
     * boolean value. e.g. logP() &lt; 5
     *
     * @param expression
     * @return
     */
    public RDKitMoleculeProcessor filter(String expression) {
        definitions.add(EvaluatorDefintion.filter(expression));
        return this;

    }

}
