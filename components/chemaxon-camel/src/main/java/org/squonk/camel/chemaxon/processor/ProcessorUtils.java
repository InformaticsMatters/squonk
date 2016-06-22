package org.squonk.camel.chemaxon.processor;

import chemaxon.formats.MolExporter;
import chemaxon.struc.Molecule;
import org.squonk.util.CloseableQueue;
import java.io.IOException;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class ProcessorUtils {

    public static String determineStringProperty(Exchange exchange, String defaultValue, String headerProperty) {
        String headerOpt = exchange.getIn().getHeader(headerProperty, String.class);
        if (headerOpt != null) {
            return headerOpt;
        } else {
            return defaultValue;
        }
    }
    
    public static Integer determineIntProperty(Exchange exchange, Integer defaultValue, String headerProperty) {
        Integer headerOpt = exchange.getIn().getHeader(headerProperty, Integer.class);
        if (headerOpt != null) {
            return headerOpt;
        } else {
            return defaultValue;
        }
    }

    public static void writeMoleculesToMolExporter(final MolExporter exporter, final Molecule[] mols) throws IOException {
        for (Molecule mol : mols) {
            exporter.write(mol);
        }
    }

    public static void writeMoleculesToQueue(final CloseableQueue q, final Molecule[] mols) throws IOException {
        for (Molecule mol : mols) {
            q.add(mol);
        }
    }

}
