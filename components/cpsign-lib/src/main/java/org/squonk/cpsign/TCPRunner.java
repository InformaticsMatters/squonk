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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.genettasoft.modeling.CPSignFactory;
import com.genettasoft.modeling.SignificantSignature;
import com.genettasoft.modeling.cheminf.api.ISignTCPClassification;
import com.genettasoft.modeling.tcp.api.ITCPImpl;

public class TCPRunner {

    CPSignFactory factory;
    File chemFile, tempTCPData, tempSignatures;


    /**
     * Parameters to play around with
     */
    int nrModels = 10;
    String smilesToPredict = "CCCNCC(=O)NC1=CC(=CC=C1)S(=O)(=O)NC2=NCCCCC2.C(=O)(C(=O)O)O";
    boolean saveTCPRecordsCompressed = true;
    boolean saveSignaturesCompressed = true;


    public static void main(String[] args) {
        TCPRunner acp = new TCPRunner();
        acp.intialise();
        acp.predictWithTCPClassification();
        System.out.println("Finished Example TCP-Classification");
    }

    /**
     * This method just initializes some variables and the CPSignFactory. Please change the
     * initialization of CPSignFactory to point to your active license. Also change the
     * model and signature-files into a location on your machine so that they can be used
     * later on, now temporary files are created for illustrative purposes.
     */
    public void intialise(){
        // Start with instantiating CPSignFactory with your license
        try {
            factory = new CPSignFactory(new FileInputStream("../../data/licenses/cpsign0.3pro.license"));
        } catch (IllegalArgumentException | IOException e) {
            writeErrAndExit("Could not load the license or it was invalid");
        }

        // Init the files

        try {
            chemFile = new File("../../data/testfiles/bursi_classification.sdf");
            tempTCPData = File.createTempFile("bursiTCP", ".csr");
            tempSignatures = File.createTempFile("bursiTCP", ".signs");
        } catch(Exception ioe){
            writeErrAndExit("Could not create temporary files for saving models to");
        }
    }

     void writeErrAndExit(String errMsg){
        System.err.println(errMsg);
        System.exit(1);
    }


    /**
     * Loads previously created models and use them to predict
     */
    public void predictWithTCPClassification() {

        // Chose the implementation of the TCP (currently only LibLinear supported)
        ITCPImpl tcpImpl = factory.createTCPLibLinear();

        // Wrap the TCP-implementation in a Signatures-wrapper
        ISignTCPClassification signTCP = factory.createSignTCPClassification(tcpImpl, 1, 3);

        // Load data from Chemical file
        try{
            signTCP.fromChemFile(chemFile, "Ames test categorisation", Arrays.asList("mutagen", "nonmutagen"));
            // or signTCP.fromPrecomputed(datastream, signstream); if precomputed before
            // or signTCP.fromMolsIterator(molsIterator); if other data sources
        } catch (IllegalAccessException | IllegalArgumentException | IOException e) {
            writeErrAndExit(e.getMessage());
        }

        // Predict a new example
        try{
            IAtomContainer testMol = CPSignFactory.parseSMILES(smilesToPredict);
            double[] pvals = signTCP.predictMondrianTwoClasses(testMol);

            System.out.println("Predicted pvals: [mutagen: " + pvals[0] + ", nonmutagen: " + pvals[1] + "]");

            // Predict the SignificantSignature
            SignificantSignature ss = signTCP.predictSignificantSignature(testMol);
            System.out.println(ss);
        } catch (CDKException | IllegalArgumentException | IllegalAccessException e){
            writeErrAndExit(e.getMessage());
        }

        // Save the generated records and signatures to file
        try{
            signTCP.writeSignatures(new FileOutputStream(tempSignatures), saveSignaturesCompressed);
            signTCP.writeData(new FileOutputStream(tempTCPData), saveTCPRecordsCompressed);
        } catch(IOException | IllegalAccessException e){
            System.out.println("Could not print records and signatures");
        }

    }

}
