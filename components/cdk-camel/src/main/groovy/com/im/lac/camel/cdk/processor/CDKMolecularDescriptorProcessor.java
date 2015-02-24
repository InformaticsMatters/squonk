package com.im.lac.camel.cdk.processor;

import com.im.lac.camel.processor.MoleculeObjectSourcer;
import com.im.lac.cdk.molecule.DescriptorCalculator;
import com.im.lac.cdk.molecule.MolecularDescriptors;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.CloseableMoleculeObjectQueue;
import com.im.lac.util.CloseableQueue;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class CDKMolecularDescriptorProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(CDKMolecularDescriptorProcessor.class.getName());

    private List<DescriptorDefinition> calculatorDefintions = new ArrayList<>();

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
        MoleculeObjectSourcer sourcer = new MoleculeObjectSourcer() {
            @Override
            public void handleSingle(Exchange exchange, MoleculeObject mo) throws Exception {
                for (DescriptorCalculator calculator : calculators) {
                    calculator.calculate(mo);
                }
                exchange.getIn().setBody(mo);
            }

            @Override
            public void handleMultiple(Exchange exchange, Iterator<MoleculeObject> mols) throws Exception {
                for (DescriptorCalculator calculator : calculators) {
                    mols = calculateMultiple(mols, calculator);
                }
                exchange.getIn().setBody(mols);
            }
        };

        sourcer.handle(exchange);
    }

    CloseableQueue<MoleculeObject> calculateMultiple(
            final Iterator<MoleculeObject> mols,
            final DescriptorCalculator calculator) {
        final CloseableQueue<MoleculeObject> q = new CloseableMoleculeObjectQueue(50);
        Thread t = new Thread(() -> {
            try {
                while (mols.hasNext()) {
                    MoleculeObject mo = mols.next();
                    try {
                        calculator.calculate(mo);
                        q.add(mo);
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Failed to evaluate molecule", ex);
                    }
                }
            } finally {
                q.close();
                if (mols instanceof Closeable) {
                    try {
                        LOG.log(Level.FINER, "Closing mols:{0}", mols);
                        ((Closeable) mols).close();
                    } catch (IOException e) {

                    }
                }
            }
        });
        t.start();

        return q;
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
