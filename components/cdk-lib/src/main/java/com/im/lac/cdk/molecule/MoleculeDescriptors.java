/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.cdk.molecule;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.HBondDonorCountDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.WienerNumbersDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.IntegerResult;

/**
 * This class is an experiment to work out how best to utilise CDK molecular descriptors
 *
 * @author timbo
 */
public class MoleculeDescriptors {

    public static final String WIENER_PATH = "CdkWienerPath";
    public static final String WIENER_POLARITY = "CdkWienerPolarity";
    public static final String ALOGP_ALOPG = "CdkALogP";
    public static final String ALOGP_ALOPG2 = "CdkALogP2";
    public static final String ALOGP_AMR = "CdkAMR";
    public static final String HBOND_ACCEPTOR_COUNT = "CdkHBondAcceptorCount";
    public static final String HBOND_DONOR_COUNT = "CdkHBondDonorCount";

    public static IAtomContainer wienerNumbers(IAtomContainer mol) {
        return wienerNumbers(mol, WIENER_PATH, WIENER_POLARITY);
    }

    public static IAtomContainer wienerNumbers(IAtomContainer mol, final String propNameWienerPath, final String propNameWienerPolarity) {
        IMolecularDescriptor descriptor = new WienerNumbersDescriptor();
        DescriptorValue result = descriptor.calculate(mol);
        DoubleArrayResult retval = (DoubleArrayResult) result.getValue();
        double wpath = retval.get(0); // Wiener path number
        double wpol = retval.get(1);  // Wiener polarity number
        mol.setProperty(propNameWienerPath, wpath);
        mol.setProperty(propNameWienerPolarity, wpol);
        return mol;
    }

    public static IAtomContainer aLogP(IAtomContainer mol) throws CDKException {
        return aLogP(mol, ALOGP_ALOPG, ALOGP_ALOPG2, ALOGP_AMR);
    }

    public static IAtomContainer aLogP(IAtomContainer mol, final String propNameALogP, final String propNameALogP2, final String propNameAMR) throws CDKException {
        IMolecularDescriptor descriptor = new ALOGPDescriptor();
        // de-hdrogenize, aromatise?
        DescriptorValue result = descriptor.calculate(mol);
        DoubleArrayResult retval = (DoubleArrayResult) result.getValue();
        mol.setProperty(propNameALogP, retval.get(0)); // ALogP
        mol.setProperty(propNameALogP2, retval.get(1)); // ALogP2
        mol.setProperty(propNameAMR, retval.get(2)); // AMR
        return mol;
    }

    //
    public static IAtomContainer hbondAcceptorCount(IAtomContainer mol) {
        return hbondAcceptorCount(mol, HBOND_ACCEPTOR_COUNT);
    }

    public static IAtomContainer hbondAcceptorCount(IAtomContainer mol, String propNameHBondAcceptorCount) {
        IMolecularDescriptor descriptor = new HBondAcceptorCountDescriptor();
        DescriptorValue result = descriptor.calculate(mol);
        IntegerResult retval = (IntegerResult) result.getValue();
        mol.setProperty(propNameHBondAcceptorCount, retval.intValue());
        return mol;
    }

    public static IAtomContainer hbondDonorCount(IAtomContainer mol) {
        return hbondDonorCount(mol, HBOND_DONOR_COUNT);
    }

    public static IAtomContainer hbondDonorCount(IAtomContainer mol, String propNameHBondDonorCount) {
        IMolecularDescriptor descriptor = new HBondDonorCountDescriptor();
        DescriptorValue result = descriptor.calculate(mol);
        IntegerResult retval = (IntegerResult) result.getValue();
        mol.setProperty(propNameHBondDonorCount, retval.intValue());
        return mol;
    }
}
