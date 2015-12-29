package org.squonk.camel.cdk.processor;

import com.im.lac.camel.processor.StreamingMoleculeObjectSourcer;
import org.squonk.cdk.molecule.DescriptorCalculator;
import org.squonk.cdk.molecule.MolecularDescriptors;
import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.MoleculeObjectDataset;
import java.util.ArrayList;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class CDKMolecularDescriptorProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(CDKMolecularDescriptorProcessor.class.getName());

    private final List<DescriptorDefinition> calculatorDefintions = new ArrayList<>();

    public CDKMolecularDescriptorProcessor calculate(MolecularDescriptors.Descriptor descriptor, String[] propNames) {
        calculatorDefintions.add(new DescriptorDefinition(descriptor, propNames));
        return this;
    }

    public CDKMolecularDescriptorProcessor calculate(MolecularDescriptors.Descriptor descriptor) {
        calculatorDefintions.add(new DescriptorDefinition(descriptor, descriptor.defaultPropNames));
        return this;
    }

    List<DescriptorCalculator> getCalculators(Exchange exchange) throws InstantiationException, IllegalAccessException {
        List<DescriptorCalculator> calculators = new ArrayList<>();
        for (DescriptorDefinition def : calculatorDefintions) {
            calculators.add(def.descriptor.create(def.propNames));
        }
        return calculators;
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        LOG.fine("Processing CDK Molecular Descriptors");
        final List<DescriptorCalculator> calculators = getCalculators(exchange);
        StreamingMoleculeObjectSourcer sourcer = new StreamingMoleculeObjectSourcer() {
            @Override
            public void handleSingle(Exchange exchange, MoleculeObject mo) throws Exception {
                for (DescriptorCalculator calculator : calculators) {
                    calculator.calculate(mo);
                }
                exchange.getIn().setBody(mo);
            }

            @Override
            public void handleMultiple(Exchange exchange, Stream<MoleculeObject> mols) throws Exception {
                for (DescriptorCalculator calculator : calculators) {
                    mols = calculateMultiple(mols, calculator);
                }
                exchange.getIn().setBody(new MoleculeObjectDataset(mols));
            }
        };

        sourcer.handle(exchange);
    }

    Stream<MoleculeObject> calculateMultiple(Stream<MoleculeObject> input, final DescriptorCalculator calculator) {
        input = input.peek((mo) -> {
            try {
                calculator.calculate(mo);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Failed to evaluate molecule", ex);
            }
        });
        return input;
    }

    class DescriptorDefinition {

        MolecularDescriptors.Descriptor descriptor;
        String[] propNames;

        DescriptorDefinition(
                MolecularDescriptors.Descriptor descriptor,
                String[] propNames) {
            this.descriptor = descriptor;
            this.propNames = propNames;

        }

    }

}
