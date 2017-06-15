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
import com.genettasoft.modeling.acp.ACPRegressionResult;
import com.genettasoft.modeling.ccp.api.ICCPRegressionImpl;
import com.genettasoft.modeling.cheminf.api.ISignCCPRegression;
import org.javatuples.Pair;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.squonk.cdk.io.CDKMoleculeIOUtils;
import org.squonk.types.BoundedValue;
import org.squonk.types.CPSignTrainResult;
import org.squonk.types.MoleculeObject;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 14/10/2016.
 */
public class CCPRegressionRunner extends AbstractCCPRunner {

    private static final Logger LOG = Logger.getLogger(CCPRegressionRunner.class.getName());



    public CCPRegressionRunner(File license, File dataDir) throws IOException {
        this(license, dataDir, CPSignTrainResult.Library.LibSVM, 1, 3);
    }

    public CCPRegressionRunner(File license, File dataDir, CPSignTrainResult.Library library, int signatureStartHeight, int signatureEndHeight) throws IOException {
        super(license, dataDir, library, signatureStartHeight, signatureEndHeight);
    }

    private ISignCCPRegression createCCPRegression() {
        ICCPRegressionImpl ccpImpl = null;
        switch (library) {
            case LibLinear:
                ccpImpl = factory.createCCPRegressionLibLinear();
                break ;
            case LibSVM:
                ccpImpl = factory.createCCPRegressionLibSVM();
                break;
        }

        return factory.createSignCCPRegression(ccpImpl, signatureStartHeight, signatureEndHeight);
    }

    public Predictor createPredictor(int numModels, String path) throws Exception {
        ISignCCPRegression signCCP = createCCPRegression();
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

                        List<ACPRegressionResult> regResults = signCCP.predict(testMol, Arrays.asList(confidence));
                        ACPRegressionResult regResult = regResults.get(0);

                        // Predict the SignificantSignature
                        SignificantSignature ss = signCCP.predictSignificantSignature(testMol);
                        //LOG.info(ss.toString());
                        mo.putValue(label + "_Prediction", new BoundedValue.Double(regResult.getY_hat(), regResult.getInterval().getValue0(), regResult.getInterval().getValue1()));
                        mo.putValue(label + "_Distance", regResult.getDistance());
                        mo.putValue(label + "_EHat", regResult.getE_hat());
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
            int cvFolds, double confidence)
            throws Exception {

        ISignCCPRegression signCCP = createCCPRegression();
        Iterator<Pair<IAtomContainer, Double>> molsIterator = createMolsIterator(mols, fieldName);
        // Load data
        signCCP.fromMolsIterator(molsIterator);

        CVResult cvr = crossValidate(signCCP, fieldName, cvFolds, confidence);
        String path = trainModels(signCCP, fieldName, cvFolds);

        return new CPSignTrainResult(CPSignTrainResult.Method.CCP, CPSignTrainResult.Type.Regression, library,
                signatureStartHeight, signatureEndHeight, cvFolds,
                cvr.getEfficiency(), cvr.getValidity(), cvr.getRmse(),
                path);
    }


    public CVResult crossValidate(
            ISignCCPRegression signCCP, String fieldName,
            int cvFolds, double confidence)
            throws IllegalArgumentException, IOException, IllegalAccessException {

        //Do cross-validation with cvFolds nr of folds
        CVResult result = signCCP.cross_validate(cvFolds, confidence);
        LOG.info("Cross-validation with " + cvFolds + " folds and confidence " + confidence + ": " + result);

        return result;
    }


    public String trainModels(
            ISignCCPRegression signCCP, String fieldName,
            int cvFolds)
            throws IllegalAccessException, IOException {

        // Train the Cross-Conformal Prediction problem
        signCCP.trainCCP(cvFolds);

        String path = UUID.randomUUID().toString();
        File dir = new File(dataDir, path);
        if (!dir.mkdir()) {
            throw new IOException("Could not create work dir " + dataDir + "/" + path);
        }
        signCCP.writeModelFiles(new File(dir, modelFilebase), compress);
        signCCP.writeSignatures(new FileOutputStream(new File(dir, signaturesFilename)), compress);

        return path;
    }

}
