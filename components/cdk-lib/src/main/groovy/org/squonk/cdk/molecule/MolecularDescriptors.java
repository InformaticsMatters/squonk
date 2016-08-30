package org.squonk.cdk.molecule;

import org.squonk.cdk.io.CDKMoleculeIOUtils;
import static org.squonk.cdk.molecule.CDKMoleculeUtils.AROMATICITY;
import static org.squonk.cdk.molecule.CDKMoleculeUtils.CDK_MOLECULE_WITH_ALL_EXPLICIT_HYDROGENS;
import static org.squonk.cdk.molecule.CDKMoleculeUtils.CDK_MOLECULE_WITH_ALL_IMPLICIT_HYDROGENS;
import static org.squonk.cdk.molecule.CDKMoleculeUtils.HYDROGEN_ADDER;
import org.squonk.types.MoleculeObject;
import java.io.IOException;
import java.util.logging.Logger;

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
import org.squonk.util.IOUtils;

/**
 * This class is an experiment to work out how best to utilise CDK molecular
 * descriptors
 *
 * @author timbo
 */
public class MolecularDescriptors {

    private static final Logger LOG = Logger.getLogger(MolecularDescriptors.class.getName());

    public static final String STATS_PREFIX = "CDK";

    public static final String WIENER_PATH = "WienerPath_CDK";
    public static final String WIENER_POLARITY = "WienerPolarity_CDK";
    public static final String ALOGP_ALOPG = "ALogP_CDK";
    public static final String ALOGP_AMR = "AMR_CDK";
    public static final String XLOGP_XLOGP = "XLogP_CDK";
    public static final String HBOND_ACCEPTOR_COUNT = "HBA_CDK";
    public static final String HBOND_DONOR_COUNT = "HBD_CDK";

    public enum Descriptor {

        ALogP(ALogPCalculator.class, new String[]{ALOGP_ALOPG,ALOGP_AMR}, new Class[] {Double.class,Double.class}),
        XLogP(XLogPCalculator.class, new String[]{XLOGP_XLOGP}, new Class[] {Double.class}),
        HBondDonorCount(HBondDonorCountCalculator.class, new String[]{HBOND_DONOR_COUNT}, new Class[] {Integer.class}),
        HBondAcceptorCount(HBondAcceptorCountCalculator.class, new String[]{HBOND_ACCEPTOR_COUNT}, new Class[] {Integer.class}),
        WienerNumbers(WienerNumberCalculator.class, new String[]{WIENER_PATH,WIENER_POLARITY}, new Class[] {Double.class,Double.class});

        public Class implClass;
        public String[] defaultPropNames;
        public Class[] propTypes;

        Descriptor(Class cls, String[] defaultPropNames, Class[] propTypes) {
            assert defaultPropNames.length == propTypes.length;
            this.implClass = cls;
            this.defaultPropNames = defaultPropNames;
            this.propTypes = propTypes;
        }

        public DescriptorCalculator create(String[] propNames) throws InstantiationException, IllegalAccessException {
            DescriptorCalculator inst = (DescriptorCalculator) implClass.newInstance();
            inst.key = this.toString();
            inst.propNames = propNames;
            inst.propTypes = propTypes;
            return inst;
        }
    }

    private static IAtomContainer getMoleculeWithExplicitHydrogens(MoleculeObject mo, boolean save) throws IOException, CDKException, CloneNotSupportedException, IOException, CDKException, IOException {
        IAtomContainer result = mo.getRepresentation(CDK_MOLECULE_WITH_ALL_EXPLICIT_HYDROGENS, IAtomContainer.class);
        if (result == null) {
            IAtomContainer mol = getMolecule(mo, save);
            if (mol != null) {
                result = CDKMoleculeUtils.moleculeWithExlicitHydrogens(mol);
                if (save) {
                    mo.putRepresentation(CDK_MOLECULE_WITH_ALL_EXPLICIT_HYDROGENS, result);
                }
            }
        }
        return result;
    }

    /**
     * Finds or builds the default CDK IAtomContainer that has atom
     * types configured, implicit hydrogens added and aromaticity detected
     *
     * @param mo
     * @param save
     * @return
     * @throws IOException
     * @throws CDKException
     * @throws CloneNotSupportedException
     */
    private static IAtomContainer getMolecule(MoleculeObject mo, boolean save)
            throws IOException, CDKException, CloneNotSupportedException {

        IAtomContainer result = mo.getRepresentation(CDK_MOLECULE_WITH_ALL_IMPLICIT_HYDROGENS, IAtomContainer.class);
        if (result == null) {
            IAtomContainer mol = mo.getRepresentation(IAtomContainer.class.getName(), IAtomContainer.class);
            if (mol == null) {
                mol = CDKMoleculeIOUtils.readMolecule(mo.getSource());
            }
            if (mol != null) {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
                mo.putRepresentation(IAtomContainer.class.getName(), mol.clone());

                HYDROGEN_ADDER.addImplicitHydrogens(mol);
                AROMATICITY.apply(mol);
                result = mol;
                if (save) {
                    mo.putRepresentation(CDK_MOLECULE_WITH_ALL_IMPLICIT_HYDROGENS, result);
                }
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
        aLogP(mo, ALOGP_ALOPG, ALOGP_AMR);
    }

    public static void aLogP(MoleculeObject mo, String propNameALogP, String propNameAMR)
            throws Exception {
        DescriptorCalculator calc = new ALogPCalculator();
        calc.propNames = new String[]{propNameALogP, propNameAMR};
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
            if (mo.getSource() != null) {
                IAtomContainer mol = prepareMolecule(mo);
                String prop = propNames[0];
                if (mol != null) {
                    DescriptorValue result = descriptor.calculate(mol);
                    IntegerResult retval = (IntegerResult) result.getValue();
                    mo.putValue(prop, retval.intValue());
                    incrementExecutionCount(1);
                } else {
                    LOG.info("Failed to prepare molecule " + mo + " for calculating " + propNames[0]);
                }
            }
        }
    }

    abstract static class DoubleCalculator extends DescriptorCalculator {

        @Override
        public void calculate(MoleculeObject mo) throws Exception {
            if (mo.getSource() != null) {
                IAtomContainer mol = prepareMolecule(mo);
                String prop = propNames[0];
                if (mol != null) {
                    DescriptorValue result = descriptor.calculate(mol);
                    DoubleResult retval = (DoubleResult) result.getValue();
                    if (retval != null) {
                        putDoubleIfProperValue(mo, prop, retval.doubleValue());
                        incrementExecutionCount(1);
                    }
                } else {
                    LOG.info("Failed to prepare molecule " + mo + " for calculating " + propNames[0]);
                }
            }
        }
    }

    abstract static class DoubleArrayCalculator extends DescriptorCalculator {

        @Override
        public void calculate(MoleculeObject mo) throws Exception {
            if (mo.getSource() != null) {
                IAtomContainer mol = prepareMolecule(mo);
                if (mol != null) {
                    DescriptorValue result = descriptor.calculate(mol);
                    DoubleArrayResult retval = (DoubleArrayResult) result.getValue();
                    if (retval != null) {
                        for (int i = 0; i < propNames.length; i++) {
                            String prop = propNames[i];
                            putDoubleIfProperValue(mo, prop, retval.get(i));
                        }
                        incrementExecutionCount(1);
                    }
                } else {
                    LOG.info("Failed to prepare molecule " + mo + " for calculating " + IOUtils.joinArray(propNames, ","));
                }
            }
        }
    }

    private static void putDoubleIfProperValue(MoleculeObject mo, String propName, Double d) {
        if (d != null && !d.isNaN() && !d.isInfinite()) {
            mo.putValue(propName, d);
        }
    }

    static class HBondDonorCountCalculator extends IntegerCalculator {

        HBondDonorCountCalculator() {
            descriptor = new HBondDonorCountDescriptor();
            descriptions = new String[]{"H-bond donor count"};
        }

        @Override
        public IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception {
            return getMoleculeWithExplicitHydrogens(mo, true);
        }
    }

    static class HBondAcceptorCountCalculator extends IntegerCalculator {

        HBondAcceptorCountCalculator() {
            descriptor = new HBondAcceptorCountDescriptor();
            descriptions = new String[]{"H-bond acceptor count"};
        }

        @Override
        public IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception {
            return getMoleculeWithExplicitHydrogens(mo, true);
        }
    }

    static class WienerNumberCalculator extends DoubleArrayCalculator {

        WienerNumberCalculator() {
            descriptor = new WienerNumbersDescriptor();
            descriptions = new String[]{"Wiener path", "Wiener polarity"};
        }

        @Override
        public IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception {
            return getMoleculeWithExplicitHydrogens(mo, true);
        }
    }

    static class ALogPCalculator extends DoubleArrayCalculator {

        ALogPCalculator() throws CDKException {
            descriptor = new ALOGPDescriptor();
            descriptions = new String[]{"ALogP", "AMR"};
        }

        @Override
        public IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception {
            return getMoleculeWithExplicitHydrogens(mo, true);
        }
    }

    static class XLogPCalculator extends DoubleCalculator {

        public XLogPCalculator() {
            descriptor = new XLogPDescriptor();
            descriptions = new String[]{"XLogP"};
        }

        @Override
        public IAtomContainer prepareMolecule(MoleculeObject mo) throws Exception {
            return getMoleculeWithExplicitHydrogens(mo, true);
        }
    }

}
