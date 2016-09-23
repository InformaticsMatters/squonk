package org.squonk.camel.chemaxon.processor;

import org.squonk.camel.processor.StreamingMoleculeObjectSourcer;
import org.squonk.chemaxon.molecule.StandardizerEvaluator;
import org.squonk.types.MoleculeObject;
import org.squonk.dataset.MoleculeObjectDataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.IOHelper;

/**
 *
 * @author timbo
 */
public class StandardizerProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(StandardizerProcessor.class.getName());

    private final StandardizerEvaluator evaluator;

    public StandardizerProcessor(String config) {
        evaluator = new StandardizerEvaluator(config, 25);
    }

    public StandardizerProcessor(InputStream input) throws IOException {
        this(IOHelper.loadText(input));
    }

    public StandardizerProcessor(File file) throws FileNotFoundException, IOException {
        this(new FileInputStream(file));
    }

    public StandardizerProcessor(URL url) throws IOException {
        this(url.openStream());
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        // TODO - handle the metrics
        final Map<String,Integer> stats = new HashMap<>();
        StreamingMoleculeObjectSourcer sourcer = new StreamingMoleculeObjectSourcer() {
            @Override
            public void handleSingle(Exchange exchange, MoleculeObject mo) throws IOException {
                MoleculeObject neu = standardizeMolecule(exchange, mo, stats);
                exchange.getIn().setBody(neu);
            }

            @Override
            public void handleMultiple(Exchange exchange, Stream<MoleculeObject> mols) {
                Stream<MoleculeObject> s = standardizeMultiple(exchange, mols, stats);
                exchange.getIn().setBody(new MoleculeObjectDataset(s));
            }
        };
        sourcer.handle(exchange);
    }

    MoleculeObject standardizeMolecule(Exchange exchange, MoleculeObject mo, Map<String,Integer> stats) throws IOException {
        return evaluator.processMoleculeObject(mo, stats);
    }

    Stream<MoleculeObject> standardizeMultiple(final Exchange exchange, final Stream<MoleculeObject> mols, Map<String,Integer> stats) {

        return mols.map((mo) -> {
            try {
                return standardizeMolecule(exchange, mo, stats);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
