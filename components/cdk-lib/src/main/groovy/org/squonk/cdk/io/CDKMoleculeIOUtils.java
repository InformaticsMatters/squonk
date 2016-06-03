package org.squonk.cdk.io;

import com.im.lac.types.MoleculeObject;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.*;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.squonk.types.CDKSDFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author timbo
 */
public class CDKMoleculeIOUtils {

    private static final Logger LOG = Logger.getLogger(CDKMoleculeIOUtils.class.getName());


    public static Iterator<IAtomContainer> moleculeIterator(final InputStream is)
            throws IOException, CDKException {
        Iterable iter = moleculeIterable(is);
        return iter.iterator();
    }

    public static Iterable<IAtomContainer> moleculeIterable(InputStream is)
            throws IOException, CDKException {
        BufferedInputStream bis = new BufferedInputStream(is);
        bis.mark(10000);
        IChemFormat format = new FormatFactory().guessFormat(bis);
        bis.reset();

        ISimpleChemObjectReader reader;
        if (format == null) {
            // let's try smiles as FormatFactory cannot detect that
            //System.out.println("Trying as smiles");
            reader = new SMILESReader();
        } else {
            //System.out.println("Trying as " + format.getReaderClassName());
            try {
                reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new CDKException("Failed to instantiate reader", e);
            }
        }
        reader.setReader(bis);

        ChemFile chemFile = reader.read(new ChemFile());
        List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
        return containersList;
    }

    public static IAtomContainer fetchMolecule(MoleculeObject mo, boolean store) {
        IAtomContainer mol = mo.getRepresentation(IAtomContainer.class.getName(), IAtomContainer.class);
        try {
            if (mol == null) {
                mol = CDKMoleculeIOUtils.readMolecule(mo.getSource());
            }
            if (mol != null) {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
                if (store) {
                    mo.putRepresentation(IAtomContainer.class.getName(), mol);
                }
                return mol.clone();
            }
        } catch (CDKException | CloneNotSupportedException e) {
            LOG.log(Level.INFO, "CDK unable to generate molecule: " + e.getMessage());
        }
        return null;
    }

    public static IAtomContainer readMolecule(String mol) throws CDKException {

        if (mol == null) {
            return null;
        }

        ISimpleChemObjectReader reader = null;
        FormatFactory factory = new FormatFactory();
        try {
            IChemFormat format = factory.guessFormat(new StringReader(mol));
            if (format == null) {
                if (mol.startsWith("InChI=")) {
                    reader = new INChIReader();
                } else {
                    // give up and assume smiles
                    SmilesParser parser = new SmilesParser(SilentChemObjectBuilder.getInstance());
                    return parser.parseSmiles(mol);
                }
            } else {
                reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | IOException ex) {
            throw new CDKException("Failed to create reader", ex);
        }
        if (reader != null) {
            reader.setReader(new ByteArrayInputStream(mol.getBytes()));
            return reader.read(new AtomContainer());
        }
        return null;
    }

//    public static ISimpleChemObjectReader createReader(InputStream is)
//            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, CDKException {
//        BufferedInputStream bis = new BufferedInputStream(is);
//        bis.mark(10000);
//        IChemFormat format = new FormatFactory().guessFormat(bis);
//        bis.reset();
//        ISimpleChemObjectReader reader;
//        if (format == null) {
//            // let's try smiles as FormatFactory cannot detect that
//            System.out.println("Trying as smiles");
//            reader = new SMILESReader();
//        } else {
//            System.out.println("Trying as " + format.getReaderClassName());
//            reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
//        }
//        reader.setReader(bis);
//        return reader;
//    }

//    public static ISimpleChemObjectReader createReader(String input) throws CDKException {
//        FormatFactory factory = new FormatFactory();
//        try {
//            IChemFormat format = factory.guessFormat(new StringReader(input));
//            ISimpleChemObjectReader reader = null;
//            if (format == null) {
//                if (input.startsWith("InChI=")) {
//                    reader = new INChIReader();
//                } else {
//                    // give up and assume smiles
//                    reader = new SMILESReader();
//                }
//            } else {
//                reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
//            }
//            reader.setReader(new StringReader(input));
//            return reader;
//        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
//            throw new CDKException("Failed to create reader", ex);
//        }
//    }

    public static List<IAtomContainer> importMolecules(String s) throws CDKException {
        Iterable<IAtomContainer> iter = null;
        try {
            iter = moleculeIterable(new ByteArrayInputStream(s.getBytes()));
        } catch (IOException e) {
            throw new CDKException("Failed to read molecules", e);
        }
        List<IAtomContainer> mols = new ArrayList<>();
        if (iter != null) {
            for (IAtomContainer mol : iter) {
                mols.add(mol);
            }
        }
        return mols;
    }

    public static CDKSDFile covertToSDFile(Stream<MoleculeObject> mols, boolean haltOnError) throws IOException, CDKException {
        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);

        Thread t = new Thread() {
            public void run() {
                try (SDFWriter writer = new SDFWriter(out)) {
                    mols.forEachOrdered((mo) -> {
                        try {
                            IAtomContainer mol = fetchMolecule(mo, false);
                            for (Map.Entry<String, Object> e : mo.getValues().entrySet()) {
                                String key = e.getKey();
                                Object val = e.getValue();
                                if (key != null && val != null) {
                                    mol.setProperty(key, val);
                                }
                            }
                            // for some reason CDK adds this property with no value, so we remove it
                            mol.removeProperty("cdk:Title");
                            //LOG.info("WRITING MOL");
                            writer.write(mol);
                        } catch (CDKException e) {
                            if (haltOnError) {
                                throw new RuntimeException("Failed to read molecule " + mo.getUUID(), e);
                            } else {
                                LOG.warning("Failed to read molecule " + mo.getUUID());
                            }
                        }
                    });
                    LOG.fine("Writing to SDF complete");
                    mols.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create SDFWriter", e);
                }
            }
        };
        t.start();

        return new CDKSDFile(in);
    }


}
