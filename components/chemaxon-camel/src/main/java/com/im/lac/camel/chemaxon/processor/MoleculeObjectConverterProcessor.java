package com.im.lac.camel.chemaxon.processor;

import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Processor that does its best to generate a MoleculeObject or a
 * MoleculeObjectIterable from the body.
 *
 * @author timbo
 */
public class MoleculeObjectConverterProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectConverterProcessor.class.getName());

    @Override
    public void process(Exchange exchange) throws Exception {
        Object body = exchange.getIn().getBody();
        LOG.log(Level.FINE, "Body is {0}", body.getClass().getName());

        if (body instanceof MoleculeObjectIterable
                || body instanceof MoleculeObject
                || body instanceof Iterable
                || body instanceof Iterator) {
            LOG.fine("Returning body as is");
            return;
        }
        InputStream is = exchange.getContext().getTypeConverter().tryConvertTo(InputStream.class, exchange, body);
        if (is != null) {
            MoleculeObjectIterable mols = MoleculeObjectUtils.createIterable(is);
            exchange.getIn().setBody(mols);
            LOG.log(Level.FINE, "Returning body as iterator {0}", mols);
            return;
        }
        MoleculeObject mol = exchange.getContext().getTypeConverter().tryConvertTo(MoleculeObject.class, exchange, body);
        if (mol != null) {
            exchange.getIn().setBody(mol);
            LOG.log(Level.FINE, "Returning body as Molecule {0}", mol);
            return;
        }
        String s = exchange.getContext().getTypeConverter().tryConvertTo(String.class, exchange, body);
        if (s != null) {
            MoleculeObject mo = new MoleculeObject(s);
            exchange.getIn().setBody(mo);
            LOG.log(Level.FINE, "Returning body as MoleculeObject {0}", mo);
            return;
        }
        throw new IllegalArgumentException("Cannot obtain source of Molecules from " + body.getClass().getName());
    }

}
