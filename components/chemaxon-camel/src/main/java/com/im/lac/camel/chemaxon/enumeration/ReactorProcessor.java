package com.im.lac.camel.chemaxon.enumeration;

import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.enumeration.ReactorExecutor;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class ReactorProcessor implements Processor {

    private int ignoreRules = 0;

    public ReactorProcessor ignoreRules(int ignoreRules) {
        this.ignoreRules = ignoreRules;
        return this;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Molecule reaction = readReaction(exchange);
        if (reaction == null) {
            throw new IllegalStateException("Could not read reaction. Must be set as body or as header property named 'Reaction'");
        }
        Molecule[][] reactants = readReactants(exchange);
        if (reactants == null || reactants.length == 0) {
            throw new IllegalStateException("Could not read reactants. Must be set as header property named 'Reactants1', 'Reactants2' ...'");
        }
        ReactorExecutor.Output output = readOutputType(exchange);
        ReactorExecutor exec = new ReactorExecutor();
        Stream<MoleculeObject> results = exec.enumerate(reaction, output, ignoreRules, reactants);
        exchange.getIn().setBody(results);
    }

    Molecule[][] readReactants(Exchange exchange) throws MalformedURLException, IOException {
        String header;
        List<Molecule[]> reactants = new ArrayList<>();
        int i = 1;
        while ((header = exchange.getIn().getHeader("Reactants" + i, String.class)) != null) {
            URL url = new URL(header);
            InputStream is = url.openStream();
            try (Stream<MoleculeObject> stream = MoleculeObjectUtils.createStreamProvider(is).getStream(true)) {
                Molecule[] mols = stream
                        .map(mo -> MoleculeUtils.cloneMolecule(mo, true))
                        .collect(Collectors.toList()).toArray(new Molecule[0]);

                reactants.add(mols);
                i++;
            }
        }
        return reactants.toArray(new Molecule[0][0]);
    }

    ReactorExecutor.Output readOutputType(Exchange exchange) {
        String header = exchange.getIn().getHeader("OutputType", String.class);
        if (header == null) {
            return ReactorExecutor.Output.Product1;
        } else {
            return ReactorExecutor.Output.valueOf(header);
        }
    }

    Molecule readReaction(Exchange exchange) throws MalformedURLException, IOException {
        String header = exchange.getIn().getHeader("Reaction", String.class);
        if (header != null) {
            URL url = new URL(header);
            try (InputStream is = url.openStream()) {
                return exchange.getContext().getTypeConverter().convertTo(Molecule.class, is);
            }
        } else {
            return exchange.getIn().getBody(Molecule.class);
        }

    }
}
