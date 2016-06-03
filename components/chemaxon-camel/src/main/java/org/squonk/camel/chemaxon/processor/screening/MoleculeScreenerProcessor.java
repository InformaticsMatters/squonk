package org.squonk.camel.chemaxon.processor.screening;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import com.chemaxon.descriptors.common.Descriptor;
import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.IOHelper;
import org.squonk.chemaxon.molecule.MoleculeUtils;
import org.squonk.chemaxon.screening.MoleculeScreener;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.types.Structure;
import org.squonk.util.ExecutionStats;
import org.squonk.util.StatsRecorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Processor for performing screening.
 *
 * @author Tim Dudgeon
 * @param <T> The type of molecular descriptor
 * @see MoleculeScreener
 */
public class MoleculeScreenerProcessor<T extends Descriptor> implements Processor {

    private static final Logger LOG = Logger.getLogger(MoleculeScreenerProcessor.class.getName());

    public static final String HEADER_QUERY_MOLECULE = "structure";
    public static final String HEADER_THRESHOLD = "threshold";

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
    public void process(Exchange exch) throws Exception {

        final T targetFp = findTargetFromHeader(exch);
        if (targetFp == null && screener.getTargetDescriptor() == null) {
            throw new IllegalStateException("No target molecule found");
        }
        final double thresh = exch.getIn().getHeader(HEADER_THRESHOLD, threshold, Double.class);


        Dataset dataset = exch.getIn().getBody(Dataset.class);
        if (dataset == null || dataset.getType() != MoleculeObject.class) {
            throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
        }
        Stream<MoleculeObject> mols = dataset.getStream();

        AtomicInteger count = new AtomicInteger(0);
        Stream<MoleculeObject> results = mols.filter((mo) -> {
            if (mo.getSource() == null) {
                return false;
            }
            boolean b = false;
            try {
                double sim = compareMolecule(mo, targetFp);
                b = (sim > thresh);
                count.incrementAndGet();
                LOG.log(Level.FINER, "Similarity score = {0}", sim);
            } catch (MolFormatException ex) {
                LOG.log(Level.SEVERE, "Bad molecule format", ex);
            }
            return b;
        });

        StatsRecorder recorder = exch.getIn().getHeader(StatsRecorder.HEADER_STATS_RECORDER, StatsRecorder.class);
        if (recorder != null) {
            mols = mols.onClose(() -> {
                ExecutionStats stats = new ExecutionStats();
                stats.incrementExecutionCount("Screen_CXN", count.get());
                recorder.recordStats(stats);
            });
        }
        //handleMetadata(exch, dataset.getMetadata());
        exch.getIn().setBody(new MoleculeObjectDataset(results));
    }

    double compareMolecule(MoleculeObject query, T targetFp) throws MolFormatException {
        if (query.getSource() == null) {
            return 0d;
        }

        Molecule mol = MoleculeUtils.fetchMolecule(query, true);
        double sim = doCompare(mol, targetFp);
        if (similarityPropName != null) {
            query.putValue(similarityPropName, sim);
        }
        return sim;
    }

    Stream<MoleculeObject> compareMultiple(final Stream<MoleculeObject> mols, final T targetFp, final double thresh) {

        return mols.filter((mo) -> {
            if (mo.getSource() == null) {
                return false;
            }
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

        Structure structure = MoleculeTypeDescriptor.DISCRETE.readOptionValue(exchange.getIn().getHeaders(), HEADER_QUERY_MOLECULE);

        Molecule mol = null;
        try {
            mol = MoleculeUtils.convertToMolecule(structure.getSource(), structure.getFormat());
        } catch (MolFormatException mfe) {
            LOG.log(Level.SEVERE, "Bad molecule", mfe);
        }
        if (mol != null) {
            return screener.generateDescriptor(mol);
        }
        return null;
    }
    
}
