package com.im.lac.camel.chemaxon.processor.screening;

import chemaxon.struc.Molecule;
import com.chemaxon.descriptors.common.Descriptor;
import com.im.lac.ClosableMoleculeQueue;
import com.im.lac.ClosableQueue;
import com.im.lac.camel.chemaxon.processor.MoleculeSourcer;
import com.im.lac.chemaxon.screening.MoleculeScreener;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Processor for performing screening.
 *
 * @author Tim Dudgeon
 * @param <T> The type of molecular descriptor
 * @see com.im.lac.chemaxon.screening.MoleculeScreener
 */
public class MoleculeScreenerProcessor<T extends Descriptor> implements Processor {

    private static final Logger LOG = Logger.getLogger(MoleculeScreenerProcessor.class.getName());
    public static final String HEADER_TARGET_MOLECULE = "TargetMolecule";
    public static final String HEADER_THRESHOLD = "Threshold";

    private final MoleculeScreener<T> screener;

    private String similarityPropName = "similarity";

    private double threshold = 0.7d;

    public MoleculeScreenerProcessor(MoleculeScreener<T> screener) {
        this.screener = screener;
    }

    /**
     * The name of the property for the similarity score. The Molecule will have
     * this property added containing the value of the similarity. Default is
     * "similarity". If set to null then value is not stored.
     *
     * @param name
     * @return
     */
    public MoleculeScreenerProcessor similarityPropName(String name) {
        similarityPropName = name;
        return this;
    }

    /**
     * The similarity threshold to use. Default is 0.7
     *
     * @param d
     * @return
     */
    public MoleculeScreenerProcessor threshold(double d) {
        threshold = d;
        return this;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        final T targetFp = findTargetFromHeader(exchange);
        if (targetFp == null && screener.getTargetDescriptor() == null) {
            throw new IllegalStateException("No target molecule found");
        }
        final double thresh = exchange.getIn().getHeader(HEADER_THRESHOLD, threshold, Double.class);

        MoleculeSourcer sourcer = new MoleculeSourcer() {
            @Override
            public void handleSingle(Exchange exchange, Molecule mol) {
                compareMolecule(mol, targetFp);
                exchange.getIn().setBody(mol);
            }

            @Override
            public void handleMultiple(Exchange exchange, Iterator<Molecule> mols) {
                ClosableQueue<Molecule> q = compareMultiple(mols, targetFp, thresh);
                exchange.getIn().setBody(q);
            }
        };
        sourcer.handle(exchange);
    }

    double compareMolecule(Molecule query, T targetFp) {

        double sim = doCompare(query, targetFp);
        if (similarityPropName != null) {
            query.setPropertyObject(similarityPropName, sim);
        }
        return sim;
    }

    ClosableQueue<Molecule> compareMultiple(final Iterator<Molecule> mols, final T targetFp, final double thresh) {
        final ClosableQueue<Molecule> q = new ClosableMoleculeQueue(50);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mols.hasNext()) {
                        Molecule mol = mols.next();
                        double sim = compareMolecule(mol, targetFp);
                        LOG.log(Level.FINER, "Similarity score = {0}", sim);
                        if (sim > thresh) {
                            q.add(mol);
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

    private double doCompare(Molecule query, T targetFp) {
        if (targetFp == null) {
            return screener.compare(query);
        } else {
            return screener.compare(query, targetFp);
        }
    }

    private T findTargetFromHeader(Exchange exchange) {
        Molecule header = exchange.getIn().getHeader(HEADER_TARGET_MOLECULE, Molecule.class);
        if (header != null) {
            return screener.generateDescriptor(header);
        }
        return null;
    }

}
