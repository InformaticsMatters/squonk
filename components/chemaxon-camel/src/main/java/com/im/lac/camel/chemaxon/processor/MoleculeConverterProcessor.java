package com.im.lac.camel.chemaxon.processor;

import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.io.MoleculeIOUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
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
    public void process(Exchange exchange) throws Exception {
        Object body = exchange.getIn().getBody();
        LOG.info("Body is " + body.getClass().getName());

        if (body instanceof Molecule || body instanceof Iterator || body instanceof Iterable) {
            LOG.info("Returning body as is");
            return;
        }
        InputStream is = exchange.getContext().getTypeConverter().tryConvertTo(InputStream.class, exchange, body);
//        InputStream is = null;
//        if (body instanceof InputStream) {
//            is = (InputStream) body;
//        } else if (body instanceof String) {
//            is = new ByteArrayInputStream(((String) body).getBytes());
//        } else if (body instanceof File) {
//            is = new FileInputStream((File) body);
//        }
        if (is != null) {
            Iterator<Molecule> mols;
            mols = MoleculeIOUtils.moleculeIterator(is);
            exchange.getIn().setBody(mols);
            LOG.log(Level.INFO, "Returning body as iterator {0}", mols);
            return;
        }
        Molecule mol = exchange.getContext().getTypeConverter().tryConvertTo(Molecule.class, exchange, body);
        if (mol != null) {
            exchange.getIn().setBody(mol);
            LOG.log(Level.INFO, "Returning body as Molecule {0}", mol);
            return;
        }
        throw new IllegalArgumentException("Cannot obtain source of Molecules from " + body.getClass().getName());
    }

}
