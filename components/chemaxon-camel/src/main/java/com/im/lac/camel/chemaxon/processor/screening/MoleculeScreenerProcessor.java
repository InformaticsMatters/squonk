package com.im.lac.camel.chemaxon.processor.screening;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import com.chemaxon.descriptors.common.Descriptor;
import com.im.lac.camel.processor.StreamingMoleculeObjectSourcer;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import com.im.lac.chemaxon.screening.MoleculeScreener;
import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.MoleculeObjectDataset;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.IOHelper;

/**
 * Processor for performing screening.
 *
 * @author Tim Dudgeon
 * @param <T> The type of molecular descriptor
 * @see com.im.lac.chemaxon.screening.MoleculeScreener
 */
public class MoleculeScreenerProcessor<T extends Descriptor> implements Processor {

    private static final Logger LOG = Logger.getLogger(MoleculeScreenerProcessor.class.getName());
    public static final String HEADER_QUERY_MOLECULE = "QueryMolecule";
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

    public MoleculeScreenerProcessor standardizer(String szr) {
        screener.setStandardizer(szr);
        return this;
    }

    public MoleculeScreenerProcessor standardizer(File file) throws IOException {
        String szr = IOHelper.loadText(new FileInputStream(file));
        screener.setStandardizer(szr);
        return this;
    }

    public MoleculeScreenerProcessor clearStandardizer() {
        screener.setStandardizer(null);
        return this;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        final T targetFp = findTargetFromHeader(exchange);
        if (targetFp == null && screener.getTargetDescriptor() == null) {
            throw new IllegalStateException("No target molecule found");
        }
        final double thresh = exchange.getIn().getHeader(HEADER_THRESHOLD, threshold, Double.class);

        StreamingMoleculeObjectSourcer sourcer = new StreamingMoleculeObjectSourcer() {
            @Override
            public void handleSingle(Exchange exchange, MoleculeObject mo) throws MolFormatException {

                compareMolecule(mo, targetFp);
                exchange.getIn().setBody(mo);
            }

            @Override
            public void handleMultiple(Exchange exchange, Stream<MoleculeObject> mols) {
                Stream<MoleculeObject> stream = compareMultiple(mols, targetFp, thresh);
                exchange.getIn().setBody(new MoleculeObjectDataset(stream));
            }
        };
        sourcer.handle(exchange);
    }

    double compareMolecule(MoleculeObject query, T targetFp) throws MolFormatException {
        Molecule mol = MoleculeUtils.fetchMolecule(query, true);
        double sim = doCompare(mol, targetFp);
        if (similarityPropName != null) {
            query.putValue(similarityPropName, sim);
        }
        return sim;
    }

    Stream<MoleculeObject> compareMultiple(final Stream<MoleculeObject> mols, final T targetFp, final double thresh) {

        return mols.filter((mo) -> {
            boolean b = false;
            try {
                double sim = compareMolecule(mo, targetFp);
                b = (sim > thresh);
                LOG.log(Level.FINER, "Similarity score = {0}", sim);
            } catch (MolFormatException ex) {
                LOG.log(Level.SEVERE, "Bad molecule format", ex);
            }
            return b;
        });
    }

    private double doCompare(Molecule query, T targetFp) {
        if (targetFp == null) {
            return screener.compare(query);
        } else {
            return screener.compare(query, targetFp);

        }
    }

    private T findTargetFromHeader(Exchange exchange) {
        Object h = exchange.getIn().getHeader(HEADER_QUERY_MOLECULE);
        LOG.log(Level.FINE, "HEADER_TARGET_MOLECULE: {0}", h);
        Molecule header = exchange.getIn().getHeader(HEADER_QUERY_MOLECULE, Molecule.class
        );
        if (header
                != null) {
            return screener.generateDescriptor(header);
        }

        return null;
    }
    
}
