/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.cpsign;

import com.genettasoft.modeling.CVResult;
import com.genettasoft.modeling.SignificantSignature;
import com.genettasoft.modeling.ccp.api.ICCPClassificationImpl;
import com.genettasoft.modeling.cheminf.api.ISignCCPClassification;
import org.javatuples.Pair;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.squonk.cdk.io.CDKMoleculeIOUtils;
import org.squonk.types.CPSignTrainResult;
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
        this(license, dataDir, CPSignTrainResult.Library.LibSVM, 1, 3);
    }

    public CCPClassifierRunner(File license, File dataDir, CPSignTrainResult.Library library, int signatureStartHeight, int signatureEndHeight) throws IOException {
        super(license, dataDir, library, signatureStartHeight, signatureEndHeight);
    }

    private ISignCCPClassification createCCPClassifier() {
        ICCPClassificationImpl ccpImpl = null;
        switch (library) {
            case LibLinear:
                ccpImpl = factory.createCCPClassificationLibLinear();
                break;
            case LibSVM:
                ccpImpl = factory.createCCPClassificationLibSVM();
                break;
        }

        return factory.createSignCCPClassification(ccpImpl, signatureStartHeight, signatureEndHeight);
    }

    public Predictor createPredictor(int numModels, String path) throws Exception {
        ISignCCPClassification signCCP = createCCPClassifier();
        File dir = new File(dataDir, path);
        if (!dir.exists()) {
            throw new FileNotFoundException("Directory with models does not exist");
        }

        // Load models previously trained
        signCCP.loadModelFiles(new File(dir, modelFilebase), numModels);
        signCCP.loadSignatures(new FileInputStream(new File(dir, signaturesFilename)));
        return new Predictor() {
            @Override
            public Stream<MoleculeObject> predict(Stream<MoleculeObject> mols, String label, double confidence) throws Exception {
                return mols.peek((mo) -> {
                    IAtomContainer testMol = CDKMoleculeIOUtils.fetchMolecule(mo, false);
                    try {

                        // Predict the SignificantSignature
                        SignificantSignature ss = signCCP.predictSignificantSignature(testMol);
                        //LOG.info(ss.toString());
                        double[] pvals = ss.getPvals();
                        LOG.fine("Predicted pvals: [1: " + pvals[0] + ", 2: " + pvals[1] + "]");
                        boolean t = pvals[0] >= confidence;
                        boolean f = pvals[1] >= confidence;
                        String r;
                        if (t && f) r = "B";
                        else if (t) r = "T";
                        else if (f) r = "F";
                        else r = "N";
                        mo.putValue(label + "_Result", r);
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


    public CPSignTrainResult train(
            List<MoleculeObject> mols, String fieldName,
            Object trueValue, Object falseValue,
            int cvFolds, double confidence)
            throws Exception {

        ISignCCPClassification signCCP = createCCPClassifier();
        Iterator<Pair<IAtomContainer, Double>> molsIterator = createMolsIterator(mols, fieldName, trueValue, falseValue);
        // Load data
        signCCP.fromMolsIterator(molsIterator);

        CVResult cvr = crossValidate(signCCP, fieldName, trueValue, falseValue, cvFolds, confidence);
        String path = trainModels(signCCP, fieldName, trueValue, falseValue, cvFolds);

        return new CPSignTrainResult(CPSignTrainResult.Method.CCP, CPSignTrainResult.Type.Classification, library,
                signatureStartHeight, signatureEndHeight, cvFolds,
                cvr.getEfficiency(), cvr.getValidity(), null,
                path);
    }


    public CVResult crossValidate(
            ISignCCPClassification signCCP, String fieldName,
            Object trueValue, Object falseValue,
            int cvFolds, double confidence)
            throws IllegalArgumentException, IOException, IllegalAccessException {

        //Do cross-validation with cvFolds nr of folds
        CVResult result = signCCP.cross_validate(cvFolds, confidence);
        LOG.info("Cross-validation with " + cvFolds + " folds and confidence " + confidence + ": " + result);

        return result;
    }


    public String trainModels(
            ISignCCPClassification signCCP, String fieldName,
            Object trueValue, Object falseValue,
            int cvFolds)
            throws IllegalAccessException, IOException {

        // Train the Cross-Conformal Prediction problem
        signCCP.trainCCP(cvFolds);

        String path = UUID.randomUUID().toString();
        File dir = new File(dataDir, path);
        if (!dir.mkdir()) {
            throw new IOException("Could not create work dir " + dir.getPath());
        }
        signCCP.writeModelFiles(new File(dir, modelFilebase), compress);
        signCCP.writeSignatures(new FileOutputStream(new File(dir, signaturesFilename)), compress);

        return path;
    }

}
