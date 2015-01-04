package com.im.lac.camel.chemaxon.processor;

import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.io.MoleculeIOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Processor that does its best to generate a Molecule or a Molecule Iterator or
 * Iterable from the input.
 *
 * @author timbo
 */
public class MoleculeConverterProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(MoleculeConverterProcessor.class.getName());

    @Override
    public void process(Exchange exchange) {
        Object body = exchange.getIn().getBody();
        LOG.info("Body is " + body.getClass().getName());

        if (body instanceof Molecule || body instanceof Iterator || body instanceof Iterable) {
            LOG.info("Returning body as is");
            return;
        }
        InputStream is = null;
        if (body instanceof InputStream) {
            is = (InputStream)body;
        } else if (body instanceof String) {
            is = new ByteArrayInputStream(((String)body).getBytes());
        }
        if (is != null) {
            Iterator<Molecule> mols;
            try {
                mols = MoleculeIOUtils.moleculeIterator(is);
                exchange.getIn().setBody(mols);
                LOG.info("Returning body as iterator " + mols);
                return;
            } catch (IOException ex) {
                throw new IllegalArgumentException("Cannot create Molecules from InputStream", ex);
            }
        }
        Molecule mol = exchange.getContext().getTypeConverter().convertTo(Molecule.class, exchange, body);
        if (mol != null) {
            exchange.getIn().setBody(mol);
            LOG.info("Returning body as Molecule " + mol);
            return;
        }
        throw new IllegalArgumentException("Cannot obtain source of Molecules");
    }
    
}
