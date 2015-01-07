package com.im.lac.camel.chemaxon.processor;

import chemaxon.standardizer.Standardizer;
import chemaxon.struc.Molecule;
import com.im.lac.ClosableQueue;

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
        MoleculeSourcer sourcer = new MoleculeSourcer() {
            @Override
            void handleSingle(Exchange exchange, Molecule mol) {
                synchronized (standardizer) {
                    standardizer.standardize(mol);
                }
                exchange.getIn().setBody(mol);
            }

            @Override
            void handleMultiple(Exchange exchange, Iterator<Molecule> mols) {
                ClosableQueue<Molecule> q = standardizeMultiple(mols);
                exchange.getIn().setBody(q);
            }
        };
        sourcer.handle(exchange);
    }

    ClosableQueue<Molecule> standardizeMultiple(final Iterator<Molecule> mols) {
        final ClosableQueue<Molecule> q = new ClosableQueue<Molecule>(50);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mols.hasNext()) {
                        Molecule mol = mols.next();
                        synchronized (standardizer) { // not sure standardizer is thread safe
                            try {
                                synchronized (standardizer) {
                                    standardizer.standardize(mol);
                                }
                                q.add(mol);
                            } catch (Exception ex) {
                                LOG.log(Level.SEVERE, "Standardization failed", ex);
                            }
                        }
                    }
                } finally {
                    q.close();
                }
            }
        });
        t.start();

        return q;
    }

}
