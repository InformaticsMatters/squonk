package org.squonk.cpsign;

import com.genettasoft.modeling.CPSignFactory;
import com.google.common.util.concurrent.AtomicDouble;
import org.javatuples.Pair;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.squonk.cdk.io.CDKMoleculeIOUtils;
import org.squonk.types.AtomPropertySet;
import org.squonk.types.CPSignTrainResult;
import org.squonk.types.MoleculeObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 21/10/2016.
 */
public class AbstractCCPRunner {

    private static final Logger LOG = Logger.getLogger(AbstractCCPRunner.class.getName());

    protected static final String signaturesFilename = "signatures.signs";
    protected static final String modelFilebase = "model";

    protected final File dataDir;
    protected final CPSignFactory factory;
    protected final CPSignTrainResult.Library library;
    protected final int signatureStartHeight;
    protected final int signatureEndHeight;
    protected boolean compress = false;

    public AbstractCCPRunner(File license, File dataDir) throws IOException {
        this(license, dataDir, CPSignTrainResult.Library.LibSVM, 1, 3);
    }

    public AbstractCCPRunner(File license, File dataDir, CPSignTrainResult.Library library, int signatureStartHeight, int signatureEndHeight) throws IOException {
        this.dataDir = dataDir;
        this.factory = new CPSignFactory(new FileInputStream(license));
        this.library = library;
        this.signatureStartHeight = signatureStartHeight;
        this.signatureEndHeight = signatureEndHeight;
    }

    protected Iterator<Pair<IAtomContainer, Double>> createMolsIterator(
            List<MoleculeObject> mols, String fieldName,
            Object trueValue, Object falseValue) {

        LOG.info("Looking for field " + fieldName + " with values " + trueValue +" and " + falseValue + " from " + mols.size() + " mols");

        Stream<Pair<IAtomContainer, Double>> pairs = mols.stream().map((mo) -> {
            Object o = mo.getValue(fieldName);
            //System.out.println("Mol: " + mo.getUUID() + " -> " + o);
            if (o == null) {
                return null;
            }
            IAtomContainer mol = CDKMoleculeIOUtils.fetchMolecule(mo, false);
            if (mol == null) {
                return null;
            } else if (o.equals(falseValue)) {
                return new Pair<>(mol, 0d);
            } else if (o.equals(trueValue)) {
                return new Pair<>(mol, 1d);
            } else {
                return null;
            }
        }).filter((p) -> p != null);

        return pairs.iterator();
    }

    protected Iterator<Pair<IAtomContainer, Double>> createMolsIterator(
            List<MoleculeObject> mols, String fieldName) {

        LOG.info("Looking for field " + fieldName + " from " + mols.size() + " mols");

        Stream<Pair<IAtomContainer, Double>> pairs = mols.stream().map((mo) -> {
            Object o = mo.getValue(fieldName);
            //System.out.println("Mol: " + mo.getUUID() + " -> " + o);
            if (o == null) {
                return null;
            }
            IAtomContainer mol = CDKMoleculeIOUtils.fetchMolecule(mo, false);
            if (mol == null) {
                return null;
            } else if (o instanceof Number) {
                double d = ((Number)o).doubleValue();
                return new Pair<>(mol, d);
            } else {
                return null;
            }
        }).filter((p) -> p != null);

        return pairs.iterator();
    }

    protected AtomPropertySet generateAtomScores(IAtomContainer testMol, Map<Integer, Double> atomValues) {

        List<Map.Entry<Integer, Double>> sortedValues = new ArrayList<>(atomValues.entrySet());
        sortedValues.sort((a, b) -> 0 - a.getValue().compareTo(b.getValue()));

        List<AtomPropertySet.Score> scores = new ArrayList<>();
        final AtomicDouble last = new AtomicDouble(Double.MAX_VALUE);
        final AtomicInteger rank = new AtomicInteger(0);
        sortedValues.forEach((e) -> {
            Integer k = e.getKey();
            Double v = e.getValue();
            if (v.doubleValue() < last.doubleValue()) {
                last.set(v);
                rank.incrementAndGet();
            }
            scores.add(AtomPropertySet.createScore(k, testMol.getAtom(k).getSymbol(), v.floatValue(), rank.get()));
        });

        return new AtomPropertySet(scores);
    }


    protected interface RegressionPredictor {

        Stream<MoleculeObject> predict(Stream<MoleculeObject> mols, String label, double confidence) throws Exception;

    }

    protected interface ClassificationPredictor {

        Stream<MoleculeObject> predict(Stream<MoleculeObject> mols, String label) throws Exception;

    }


}
