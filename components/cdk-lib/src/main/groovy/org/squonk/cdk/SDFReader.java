package org.squonk.cdk;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemSequence;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.WienerNumbersDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 *
 * @author timbo
 */
public class SDFReader {

    void read(ISimpleChemObjectReader reader, String filename) throws CDKException, FileNotFoundException {

        InputStream ins = new FileInputStream(filename);
//        MDLV2000Reader reader = new MDLV2000Reader(ins);
        reader.setReader(ins);
        ChemFile chemFile = reader.read(new ChemFile());
        
        Iterator<IChemSequence> iter = chemFile.chemSequences().iterator();
        while (iter.hasNext()) {
            IChemSequence cseq = iter.next();
            System.out.println("CSQ: " + cseq.getClass().getName());
            Iterator<IChemModel> cmodels = cseq.chemModels().iterator();
            while (cmodels.hasNext()) {
                IChemModel model = cmodels.next();
                System.out.println("  Model: " + model.getClass().getName());
            }
        }
        
        List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);

        IMolecularDescriptor descriptor = new WienerNumbersDescriptor();
        for (IAtomContainer mol : containersList) {
            //System.out.println("mol: " + mol.getClass().getName());
            System.out.println(mol.getProperties());

            DescriptorValue result = descriptor.calculate(mol);
            DoubleArrayResult retval = (DoubleArrayResult) result.getValue();
            double wpath = retval.get(0); // Wiener path number
            double wpol = retval.get(1);  // Wiener polarity number
            //System.out.println(wpath + " : " + wpol);
        }
        System.out.println("Found " + containersList.size());

    }

    public static void main(String[] args) throws Exception {

        String[] files = {
            "/Users/timbo/data/structures/kinase-sar/Nature-2008/Nature-uniquemols.sdf",
            "/Users/timbo/data/structures/sutherland/dhfr_standardized.sdf.gz",
            "/Users/timbo/data/structures/nci/nci1000.smiles",
            "/Users/timbo/data/structures/nci/nci10.smiles",
            "/Users/timbo/data/structures/maybridge/Building_blocks_GBP_fixed.sdf",
            "/Users/timbo/data/structures/maybridge/Building_blocks_GBP_fixed.sdf.gz"
        };

        SDFReader instance = new SDFReader();
        System.out.println("Reading " + files[0]);
        instance.read(new MDLV2000Reader(), files[0]);
//        System.out.println("Reading " + files[2]);
//        instance.read(new SMILESReader(), files[2]);
//        System.out.println("Reading " + files[3]);
//        instance.read(new SMILESReader(), files[3]);
    }

}
