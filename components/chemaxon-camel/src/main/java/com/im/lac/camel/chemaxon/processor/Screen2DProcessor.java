package com.im.lac.camel.chemaxon.processor;

import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.sss.screen.Similarity;
import chemaxon.struc.Molecule;
import chemaxon.struc.MoleculeGraph;
import chemaxon.util.MolHandler;
import com.im.lac.ClosableQueue;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/** Simple initial implementation of in memory similarity screening.
 * This needs to be extended to support multiple descriptors and metrics.
 *
 * @author Tim Dudgeon
 */
public class Screen2DProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(Screen2DProcessor.class.getName());

    private byte[] targetFingerprint;

    private String propName;

    private Double threshold;

    private boolean aromatize = true;

    public Screen2DProcessor aromatize(boolean aromatize) {
        this.aromatize = aromatize;
        return this;
    }
    
    public Screen2DProcessor propName(String propName) {
        this.propName = propName;
        return this;
    }

    public Screen2DProcessor threshold(Double threshold) {
        this.threshold = threshold;
        return this;
    }

    public Screen2DProcessor targetStructure(String target) throws MolFormatException {
        targetStructure(MolImporter.importMol(target));
        return this;
    }

    public Screen2DProcessor targetStructure(Molecule target) {
        target.aromatize();
        this.targetFingerprint = new MolHandler(target).generateFingerprintInBytes(16, 2, 6);
        return this;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        MoleculeSourcer sourcer = new MoleculeSourcer() {
            @Override
            void handleSingle(Exchange exchange, Molecule mol) {
                    double score = screen(mol);
                    if (propName != null) {
                        mol.setPropertyObject(propName, score);
                    }
                exchange.getIn().setBody(mol);
            }

            @Override
            void handleMultiple(Exchange exchange, Iterator<Molecule> mols) {
                ClosableQueue<Molecule> q = screenMultiple(mols);
                exchange.getIn().setBody(q);
            }
        };
        sourcer.handle(exchange);
    }

    ClosableQueue<Molecule> screenMultiple(final Iterator<Molecule> mols) {
        final ClosableQueue<Molecule> q = new ClosableQueue<Molecule>(50);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mols.hasNext()) {
                        Molecule mol = mols.next();
                        double score = screen(mol);
                        if (threshold != null) {
                            if (score <= threshold) {
                                continue;
                            }
                        }
                        if (propName != null) {
                            mol.setPropertyObject(propName, score);
                        }
                        q.add(mol);
                    }
                } finally {
                    q.close();
                }
            }
        });
        t.start();

        return q;
    }

    double screen(Molecule query) {
        if (aromatize) {
            query = query.cloneMolecule();
            query.aromatize(MoleculeGraph.AROM_BASIC);
        }
        byte[] queryFP = new MolHandler(query).generateFingerprintInBytes(16, 2, 6);
        double similarity = Similarity.getTanimoto(queryFP, targetFingerprint);
        return similarity;
    }

}
