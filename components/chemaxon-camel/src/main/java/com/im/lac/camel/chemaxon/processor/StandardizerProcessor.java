package com.im.lac.camel.chemaxon.processor;

import chemaxon.standardizer.Standardizer;
import chemaxon.struc.Molecule;
import com.im.lac.util.CloseableMoleculeObjectQueue;
import com.im.lac.util.CloseableQueue;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import com.im.lac.types.MoleculeObject;
import java.io.Closeable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class StandardizerProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(StandardizerProcessor.class.getName());

    private final Standardizer standardizer;

    public StandardizerProcessor(String config) {
        standardizer = new Standardizer(config);
    }

    public StandardizerProcessor(File file) {
        standardizer = new Standardizer(file);
    }

    public StandardizerProcessor(InputStream input) {
        standardizer = new Standardizer(input);
    }

    public StandardizerProcessor(URL url) throws IOException {
        InputStream is = url.openStream();
        try {
            standardizer = new Standardizer(is);
        } finally {
            is.close();
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        MoleculeObjectSourcer sourcer = new MoleculeObjectSourcer() {
            @Override
            public void handleSingle(Exchange exchange, MoleculeObject mo) throws IOException {
                MoleculeObject neu = standardizeMolecule(exchange, mo);
                exchange.getIn().setBody(neu);
            }

            @Override
            public void handleMultiple(Exchange exchange, Iterator<MoleculeObject> mols) {
                CloseableQueue<MoleculeObject> q = standardizeMultiple(exchange, mols);
                exchange.getIn().setBody(q);
            }
        };
        sourcer.handle(exchange);
    }

    MoleculeObject standardizeMolecule(Exchange exchange, MoleculeObject mo) throws IOException {
        Molecule mol = MoleculeUtils.fetchMolecule(mo, false);
        synchronized (standardizer) {
            standardizer.standardize(mol);
        }
        String format = mo.getFormat();
        if (format == null) {
            format = "mol";
        }
        MoleculeObject neu = MoleculeUtils.derriveMoleculeObject(mo, mol, format);

        return neu;
    }

    CloseableQueue<MoleculeObject> standardizeMultiple(final Exchange exchange, final Iterator<MoleculeObject> mols) {
        final CloseableQueue<MoleculeObject> q = new CloseableMoleculeObjectQueue(50);
        Thread t = new Thread(() -> {
            try {
                while (mols.hasNext()) {
                    MoleculeObject mo = mols.next();
                    try {
                        MoleculeObject neu = standardizeMolecule(exchange, mo);
                        q.add(neu);
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Standardization failed", ex);
                    }
                }
            } finally {
                q.close();
                if (mols instanceof Closeable) {
                    try {
                        ((Closeable) mols).close();
                    } catch (IOException ioe) {
                        LOG.log(Level.WARNING, "Failed to close iterator", ioe);
                    }
                }
            }
        });
        t.start();

        return q;
    }

}
