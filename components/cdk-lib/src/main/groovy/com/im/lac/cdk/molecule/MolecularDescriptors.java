package com.im.lac.cdk.molecule;

import com.im.lac.cdk.io.CDKMoleculeIOUtils;
import static com.im.lac.cdk.molecule.CDKMoleculeUtils.AROMATICITY;
import static com.im.lac.cdk.molecule.CDKMoleculeUtils.CDK_MOLECULE_WITH_ALL_EXPLICIT_HYDROGENS;
import static com.im.lac.cdk.molecule.CDKMoleculeUtils.CDK_MOLECULE_WITH_ALL_IMPLICIT_HYDROGENS;
import static com.im.lac.cdk.molecule.CDKMoleculeUtils.HYDROGEN_ADDER;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.util.List;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.HBondDonorCountDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.WienerNumbersDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.qsar.result.IntegerResult;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

/**
 * This class is an experiment to work out how best to utilise CDK molecular
 * descriptors
 *
 * @author timbo
 */
public class MolecularDescriptors {

    public static final String WIENER_PATH = "CDK_WienerPath";
    public static final String WIENER_POLARITY = "CDK_WienerPolarity";
    public static final String ALOGP_ALOPG = "CDK_ALogP";
    public static final String ALOGP_ALOPG2 = "CDK_ALogP2";
    public static final String ALOGP_AMR = "CDK_AMR";
    public static final String XLOGP_XLOGP = "CDK_XLogP";
    public static final String HBOND_ACCEPTOR_COUNT = "CDK_HBondAcceptorCount";
    public static final String HBOND_DONOR_COUNT = "CDK_HBondDonorCount";

    public enum Descriptor {

        ALogP(ALogPCalculator.class, new String[]{ALOGP_ALOPG,ALOGP_ALOPG2,ALOGP_AMR}),
        XLogP(XLogPCalculator.class, new String[]{XLOGP_XLOGP}),
        HBondDonorCount(HBondDonorCountCalculator.class, new String[]{HBOND_DONOR_COUNT}),
        HBondAcceptorCount(HBondAcceptorCountCalculator.class, new String[]{HBOND_ACCEPTOR_COUNT}),
        WienerNumbers(WienerNumberCalculator.class, new String[]{WIENER_PATH,WIENER_POLARITY});

        public Class implClass;
        public String[] defaultPropNames;

        Descriptor(Class cls, String[] defaultPropNames) {
            this.implClass = cls;
            this.defaultPropNames = defaultPropNames;
        }

        public DescriptorCalculator create(String[] propNames) throws InstantiationException, IllegalAccessException {
            DescriptorCalculator inst = (DescriptorCalculator) implClass.newInstance();
            inst.propNames = propNames;
            return inst;
        }
    }

    private static IAtomContainer getMoleculeWithExplicitHydrogens(MoleculeObject mo, boolean save) throws IOException, CDKException, CloneNotSupportedException, IOException, CDKException, IOException {
        IAtomContainer result = mo.getRepresentation(CDK_MOLECULE_WITH_ALL_EXPLICIT_HYDROGENS, IAtomContainer.class);
        if (result == null) {
            IAtomContainer mol = getMolecule(mo, save);
            result = CDKMoleculeUtils.moleculeWithExlicitHydrogens(mol);
            if (save) {
                mo.putRepresentation(CDK_MOLECULE_WITH_ALL_EXPLICIT_HYDROGENS, result);
            }
        }
        return result;
    }

    /**
     * Finds or builds the default CDK IAtomContainer. The default has atom
     * types configures, implicit hydrogens added and aromaticity detected
     *
     * @param mo
     * @param save
     * @return
     * @throws IOException
     * @throws CDKException
     * @throws CloneNotSupportedException
     */
    private static IAtomContainer getMolecule(MoleculeObject mo, boolean save) throws IOException, CDKException, CloneNotSupportedException {
        IAtomContainer result = mo.getRepresentation(IAtomContainer.class.getName(), IAtomContainer.class);
        if (result == null) {
            List<IAtomContainer> mols = CDKMoleculeIOUtils.importMolecules(mo.getSource());
            if (mols.size() > 0) {
                IAtomContainer mol = mols.get(0);
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
                HYDROGEN_ADDER.addImplicitHydrogens(mol);
                AROMATICITY.apply(mol);
                result = mol;
                if (save) {
                    mo.putRepresentation(CDK_MOLECULE_WITH_ALL_IMPLICIT_HYDROGENS, result);
                }
            } else {
                throw new IOException("Failed to read molecule");
            }
        }
        return result;
    }

    public static void wienerNumbers(MoleculeObject mol) throws Exception {
        wienerNumbers(mol, WIENER_PATH, WIENER_POLARITY);
    }

    public static void wienerNumbers(MoleculeObject mo, String propNameWienerPath, String propNameWienerPolarity)
            throws Exception {
        DescriptorCalculator calc = new WienerNumberCalculator();
        calc.propNames = new String[]{propNameWienerPath, propNameWienerPolarity};
        calc.calculate(mo);
    }

    public static void aLogP(MoleculeObject mo) throws Exception {
        aLogP(mo, ALOGP_ALOPG, ALOGP_ALOPG2, ALOGP_AMR);
    }

    public static void aLogP(MoleculeObject mo, String propNameALogP, String propNameALogP2, String propNameAMR)
            throws Exception {
        DescriptorCalculator calc = new ALogPCalculator();
        calc.propNames = new String[]{propNameALogP, propNameALogP2, propNameAMR};
        calc.calculate(mo);
    }

    public static void xLogP(MoleculeObject mo) throws Exception {
        xLogP(mo, XLOGP_XLOGP);
    }

    public static void xLogP(MoleculeObject mo, String propNameXLogP) throws Exception {
        DescriptorCalculator calc = new XLogPCalculator();
        calc.propNames = new String[]{propNameXLogP};
        calc.calculate(mo);
    }

    public static void hbondAcceptorCount(MoleculeObject mo) throws Exception {
        hbondAcceptorCount(mo, HBOND_ACCEPTOR_COUNT);
    }

    public static void hbondAcceptorCount(MoleculeObject mo, String propNameHBondAcceptorCount) throws Exception {
        DescriptorCalculator calc = new HBondAcceptorCountCalculator();
        calc.propNames = new String[]{propNameHBondAcceptorCount};
        calc.calculate(mo);
    }

    public static void hbondDonorCount(MoleculeObject mo) throws Exception {
        hbondDonorCount(mo, HBOND_DONOR_COUNT);
    }

    public static void hbondDonorCount(MoleculeObject mo, String propNameHBondDonorCount) throws Exception {
        DescriptorCalculator calc = new HBondDonorCountCalculator();
        calc.propNames = new String[]{propNameHBondDonorCount};
        calc.calculate(mo);
    }

    abstract static class IntegerCalculator extends DescriptorCalculator {

        @Override
        public void calculate(MoleculeObject mo) throws Exception {
            IAtomContainer mol = getMoleculeWithExplicitHydrogens(mo, true);
            DescriptorValue result = descriptor.calculate(prepareMolecule(mo));
            IntegerResult retval = (IntegerResult) result.getValue();
            mo.putValue(propNames[0], retval.intValue());
        }
    }

    abstract static class DoubleCalculator extends DescriptorCalculator {

        @Override
        public void calculate(MoleculeObject mo) throws Exception {
            DescriptorValue result = descriptor.calculate(prepareMolecule(mo));
            DoubleResult retval = (DoubleResult) result.getValue();
            mo.putValue(propNames[0], retval.doubleValue());
        }
    }

    abstract static class DoubleArrayCalculator extends DescriptorCalculator {

        @Override
        public void calculate(MoleculeObject mo) throws Exception {

            DescriptorValue result = descriptor.calculate(prepareMolecule(mo));
            DoubleArrayResult retval = (DoubleArrayResult) result.getValue();
            for (int i = 0; i < propNames.length; i++) {
                mo.putValue(propNames[i], retval.get(i)); // ALogP
            }
        }
    }

    static class HBondDonorCountCalculator extends IntegerCalculator {

        HBondDonorCountCalculator() {
            descriptor = new HBondDonorCountDescriptor();
        }

        @Override
        public IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception {
            return getMolecule(mo, true);
        }
    }

    static class HBondAcceptorCountCalculator extends IntegerCalculator {

        HBondAcceptorCountCalculator() {
            descriptor = new HBondAcceptorCountDescriptor();
        }

        @Override
        public IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception {
            return getMolecule(mo, true);
        }
    }

    static class WienerNumberCalculator extends DoubleArrayCalculator {

        WienerNumberCalculator() {
            descriptor = new WienerNumbersDescriptor();
        }

        @Override
        public IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception {
            return getMoleculeWithExplicitHydrogens(mo, true);
        }
    }

    static class ALogPCalculator extends DoubleArrayCalculator {

        ALogPCalculator() throws CDKException {
            descriptor = new ALOGPDescriptor();
        }

        @Override
        public IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception {
            return getMoleculeWithExplicitHydrogens(mo, true);
        }
    }

    static class XLogPCalculator extends DoubleCalculator {

        public XLogPCalculator() {
            descriptor = new XLogPDescriptor();
        }

        @Override
        public IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception {
            return getMoleculeWithExplicitHydrogens(mo, true);
        }
    }

}
