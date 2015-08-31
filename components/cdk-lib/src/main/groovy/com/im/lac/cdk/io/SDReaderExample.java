package com.im.lac.cdk.io;

import java.io.FileReader;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

/**
 *
 * @author timbo
 */
public class SDReaderExample {

    public static void main(String[] args) throws Exception {
        FileReader in = new FileReader(args.length == 0 ? "/Users/timbo/data/structures/Maybridge/Screening_Collection.sdf" : args[0]);
        try (IteratingSDFReader reader = new IteratingSDFReader(in, SilentChemObjectBuilder.getInstance())) {

            int count = 0;
            double sum = 0;
            long t0 = System.currentTimeMillis();
            ALOGPDescriptor descriptor = new ALOGPDescriptor();
            while (reader.hasNext()) {
                count++;
                IAtomContainer mol = (IAtomContainer) reader.next();
                AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
                //System.out.println("Mol " + mol);
                DescriptorValue value = descriptor.calculate(mol);
                DoubleArrayResult retval = (DoubleArrayResult) value.getValue();
                double val = retval.get(2);
                //System.out.println(val + " " + sum);
                //System.out.println("Result: " + retval.get(2));
                if (!"NaN".equals(new Double(val).toString())) {
                    sum += retval.get(2);
                } else {
                    System.out.println("Skipping " + val);
                }
            }
            long t1 = System.currentTimeMillis();
            System.out.println("Count = " + count + " time = " + (t1 - t0));
            System.out.println("Sum: " + sum);
        }

    }

}
