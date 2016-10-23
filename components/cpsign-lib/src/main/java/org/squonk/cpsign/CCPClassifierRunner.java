package org.squonk.cpsign;

import com.genettasoft.modeling.CVResult;
import com.genettasoft.modeling.SignificantSignature;
import com.genettasoft.modeling.ccp.api.ICCPClassificationImpl;
import com.genettasoft.modeling.cheminf.api.ISignCCPClassification;
import org.javatuples.Pair;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.squonk.cdk.io.CDKMoleculeIOUtils;
import org.squonk.types.MoleculeObject;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 14/10/2016.
 */
public class CCPClassifierRunner extends AbstractCCPRunner {

    private static final Logger LOG = Logger.getLogger(CCPClassifierRunner.class.getName());

    public CCPClassifierRunner(File license, File dataDir) throws IOException {
        this(license, dataDir, TrainResult.Library.LibSVM, 1, 3);
    }

    public CCPClassifierRunner(File license, File dataDir, TrainResult.Library library, int signatureStartHeight, int signatureEndHeight) throws IOException {
        super(license, dataDir, library, signatureStartHeight, signatureEndHeight);
    }

    private ISignCCPClassification createCCPClassifier() {
        ICCPClassificationImpl ccpImpl = null;
        switch (library) {
            case LibLinear:
                ccpImpl = factory.createCCPClassificationLibLinear();
                break ;
            case LibSVM:
                ccpImpl = factory.createCCPClassificationLibSVM();
                break;
        }

        return factory.createSignCCPClassification(ccpImpl, signatureStartHeight, signatureEndHeight);
    }

    public ClassificationPredictor createPredictor(int numModels, String path) throws Exception {
        ISignCCPClassification signCCP = createCCPClassifier();
        File dir = new File(dataDir, path);
        if (!dir.exists()) {
            throw new FileNotFoundException("Directory with models does not exist");
        }

        // Load models previously trained
        signCCP.loadModelFiles(new File(dir, modelFilebase), numModels);
        signCCP.loadSignatures(new FileInputStream(new File(dir, signaturesFilename)));
        return new ClassificationPredictor() {
            @Override
            public Stream<MoleculeObject> predict(Stream<MoleculeObject> mols, String label) throws Exception {
                return mols.peek((mo) -> {
                    IAtomContainer testMol = CDKMoleculeIOUtils.fetchMolecule(mo, false);
                    try {

                        // Predict the SignificantSignature
                        SignificantSignature ss = signCCP.predictSignificantSignature(testMol);
                        //LOG.info(ss.toString());
                        double[] pvals = ss.getPvals();
                        LOG.fine("Predicted pvals: [1: " + pvals[0] + ", 2: " + pvals[1] + "]");
                        mo.putValue(label + "_PVal_T", pvals[0]);
                        mo.putValue(label + "_PVal_F", pvals[1]);
                        mo.putValue(label + "_AtomScores", generateAtomScores(testMol, ss.getAtomValues()));
                        mo.putValue(label + "_Signature", ss.getSignature());
                    } catch (CDKException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
            }
        };
    }


    public TrainResult train(
            List<MoleculeObject> mols, String fieldName,
            Object trueValue, Object falseValue,
            int cvFolds, double confidence)
            throws Exception {

        CVResult cvr = crossValidate(mols, fieldName, trueValue, falseValue, cvFolds, confidence);
        String path = trainModels(mols, fieldName, trueValue, falseValue, cvFolds);

        return new TrainResult(TrainResult.Method.CCP, TrainResult.Type.Classification,library,
                signatureStartHeight, signatureEndHeight, cvFolds,
                cvr.getEfficiency(), cvr.getValidity(), null,
                path);
    }


    public CVResult crossValidate(
            List<MoleculeObject> mols, String fieldName,
            Object trueValue, Object falseValue,
            int cvFolds, double confidence)
            throws IllegalArgumentException, IOException, IllegalAccessException {

        ISignCCPClassification signCCP = createCCPClassifier();

        Iterator<Pair<IAtomContainer, Double>> molsIterator = createMolsIterator(mols, fieldName, trueValue, falseValue);

        // Load data
        signCCP.fromMolsIterator(molsIterator);

        //Do cross-validation with cvFolds nr of folds
        CVResult result = signCCP.cross_validate(cvFolds, confidence);
        LOG.info("Cross-validation with " + cvFolds + " folds and confidence " + confidence + ": " + result);

        return result;
    }


    public String trainModels(
            List<MoleculeObject> mols, String fieldName,
            Object trueValue, Object falseValue,
            int cvFolds)
            throws IllegalAccessException, IOException {

        ISignCCPClassification signCCP = createCCPClassifier();

        Iterator<Pair<IAtomContainer, Double>> molsIterator = createMolsIterator(mols, fieldName, trueValue, falseValue);

        // Load data
        signCCP.fromMolsIterator(molsIterator);

        // Train the Cross-Conformal Prediction problem
        signCCP.trainCCP(cvFolds);

        String path = UUID.randomUUID().toString();
        File dir = new File(dataDir, path);
        if (!dir.mkdir()) {
            throw new IOException("Could not create work dir");
        }
        signCCP.writeModelFiles(new File(dir, modelFilebase), compress);
        signCCP.writeSignatures(new FileOutputStream(new File(dir, signaturesFilename)), compress);

        return path;
    }

}
