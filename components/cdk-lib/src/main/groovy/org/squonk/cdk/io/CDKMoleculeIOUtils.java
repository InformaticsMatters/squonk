/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

package org.squonk.cdk.io;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.*;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.squonk.cdk.CDKSettings;
import org.squonk.types.CDKSDFile;
import org.squonk.types.MoleculeObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author timbo
 */
public class CDKMoleculeIOUtils {

    private static final Logger LOG = Logger.getLogger(CDKMoleculeIOUtils.class.getName());
    private static final SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());

    private static Properties WRITER_PROPERTIES = new Properties();
    static {
        // set custom property to be more tolerant of aromatic bonds
        WRITER_PROPERTIES.setProperty("WriteAromaticBondTypes", CDKSettings.WriteAromaticBondTypes);
    }


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
                mol = readMolecule(mo.getSource(), mo.getFormat());
            }
            if (mol != null) {
                mol.setProperty(CDKConstants.TITLE, mo.getUUID().toString());
                //mol.setID(mo.getUUID().toString());
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

    /**
     * Read the molecule as the specified format. The format is expected to be correct, but can be null, in which case
     * we try to automatically determine the format
     *
     * @param mol    The molecules as a string
     * @param format The format e.g smiles, mol, mol:v2
     * @return
     * @throws CDKException
     */
    public static IAtomContainer readMolecule(String mol, String format) throws CDKException {
        IAtomContainer result = null;
        if (format == null) {
            return readMolecule(mol);
        } else if (format.equals("smiles")) {
            result = smilesToMolecule(mol);
        } else if (format.equals("mol")) {
            try {
                result = v2000ToMolecule(mol);
            } catch (Exception e) {
                result = v3000ToMolecule(mol);
            }
        } else if (format.startsWith("mol:v2")) {
            result = v2000ToMolecule(mol);
        } else if (format.startsWith("mol:v3")) {
            result = v3000ToMolecule(mol);
        }
        return result;
    }

    /**
     * Read the molecule, trying to determine the correct format
     *
     * @param mol
     * @return
     * @throws CDKException
     */
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
            reader.setReader(new StringReader(mol));
            IAtomContainer m = reader.read(new AtomContainer());
            try {
                reader.close();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to close reader", e);
            }
            return m;
        }
        return null;
    }

    public static IAtomContainer v2000ToMolecule(String molfile) throws CDKException {
        MDLV2000Reader v2000Parser = new MDLV2000Reader(new ByteArrayInputStream(molfile.getBytes()));
        return v2000Parser.read(new AtomContainer());
    }

    public static IAtomContainer v3000ToMolecule(String molfile) throws CDKException {
        MDLV3000Reader v3000Parser = new MDLV3000Reader(new ByteArrayInputStream(molfile.getBytes()));
        return v3000Parser.read(new AtomContainer());
    }

    public static IAtomContainer smilesToMolecule(String smiles) throws CDKException {
        return smilesParser.parseSmiles(smiles);
    }

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

    public static SDFWriter createSDFWriter(OutputStream out) {
        return configureSDFWriter(new SDFWriter(out));
    }

    public static SDFWriter createSDFWriter(BufferedWriter writer) {
        return configureSDFWriter(new SDFWriter(writer));
    }

    private static SDFWriter configureSDFWriter(SDFWriter sdfwriter) {
        // set custom properties
        sdfwriter.addChemObjectIOListener(new PropertiesListener(WRITER_PROPERTIES));
        sdfwriter.customizeJob();
        return sdfwriter;
    }

    public static MDLV2000Writer createMDLV2000Writer(Writer out) {
        MDLV2000Writer writer = new MDLV2000Writer(out);
        writer.addChemObjectIOListener(new PropertiesListener(WRITER_PROPERTIES));
        writer.customizeJob();
        return writer;
    }

    public static MDLV3000Writer createMDLV3000Writer(Writer out) {
        MDLV3000Writer writer = new MDLV3000Writer(out);
        writer.addChemObjectIOListener(new PropertiesListener(WRITER_PROPERTIES));
        return writer;
    }

    public static CDKSDFile covertToSDFile(Stream<MoleculeObject> mols, boolean haltOnError) throws IOException, CDKException {

        LOG.info("Converting to SDF");

        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        final BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(out));

        Thread t = new Thread() {
            public void run() {

                LOG.fine("Running SDF conversion");
                try (SDFWriter writer = createSDFWriter(bwriter)) {
                    // TODO - change this to a map (MoleculeObject -> IAtomContainer operation followed by an operation
                    // to write to SDF
                    AtomicInteger count = new AtomicInteger(0);
                    mols.forEachOrdered((mo) -> {
                        if (count.incrementAndGet() % 1000 == 0) {
                            LOG.info("Processed " + count.get() + " mols");
                        }
                        IAtomContainer mol = fetchMolecule(mo, false);
                        if (mol == null) {
                            if (haltOnError) {
                                String msg = "Failed to read molecule " + mo.getUUID();
                                if (mo.getFormat().startsWith("smiles")) {
                                    msg += ". Original smiles: " + mo.getSource();
                                }
                                throw new RuntimeException(msg);
                            } else {
                                LOG.warning("Failed to read molecule " + mo.getUUID());
                                mol = handleErrorWithEmptyMolecule(mo, null);
                            }
                        } else {
                            copyPropertiesToIAtomContainer(mo, mol);
                        }
                        try {
                            writer.write(mol);
                            bwriter.flush();
                        } catch (CDKException | IOException e) {
                            if (haltOnError) {
                                String msg = "Failed to write molecule " + mo.getUUID();
                                if (mo.getFormat().startsWith("smiles")) {
                                    msg += ". Original smiles: " + mo.getSource();
                                }
                                throw new RuntimeException(msg, e);
                            } else {
                                LOG.warning("Failed to write molecule " + mo.getUUID());
                                AtomContainer emptyMol = handleErrorWithEmptyMolecule(mo, e.getLocalizedMessage());
                                try {
                                    writer.write(emptyMol);
                                    bwriter.flush();
                                } catch (CDKException | IOException e1) {
                                    LOG.log(Level.WARNING, "Failed to write empty molecule " + mo.getUUID(), e1);
                                }
                            }
                        }
                    });
                    LOG.info("Writing to SDF complete");
                    mols.close();
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create SDFWriter", e);
                }
            }
        };
        t.start();

        return new CDKSDFile(in, false);
    }

    /**
     * Replace with an empty molecule. See #20
     *
     * @param mo
     * @param errorMessage
     */
    private static AtomContainer handleErrorWithEmptyMolecule(MoleculeObject mo, String errorMessage) {
        AtomContainer emptyMol = new AtomContainer();
        StringBuilder b = new StringBuilder("Invalid molecule encountered\n");
        b.append("Molecule is replaced with an empty molecule\n");
        if (mo.getFormat().startsWith("smiles")) {
            b.append("Original smiles: ").append(mo.getSource()).append("\n");
        }
        if (errorMessage != null) {
            b.append("Error was: " + errorMessage).append("\n");
        }
        copyPropertiesToIAtomContainer(mo, emptyMol);
        emptyMol.setProperty(MoleculeObject.INVALID_FORMAT_PROP, b.toString());
        return emptyMol;
    }

    private static void copyPropertiesToIAtomContainer(MoleculeObject mo, IAtomContainer iac) {
        for (Map.Entry<String, Object> e : mo.getValues().entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            if (key != null && val != null) {
                iac.setProperty(key, val);
            }
        }
        // for some reason CDK adds this property with no value, so we remove it
        //mol.removeProperty("cdk:Title");
        iac.removeProperty("cdk:Remark");
    }

    public static Stream<MoleculeObject> convertMoleculeObjects(Stream<MoleculeObject> input, String toFormat) throws CDKException {
        if (toFormat != null) {
            throw new IllegalArgumentException("toFormat must be specified");
        }
        Stream<MoleculeObject> results = null;
        if (toFormat.equals("smiles")) {
            final SmilesGenerator generator = new SmilesGenerator(SmiFlavor.Absolute);
            results = input.map(mo -> {
                try {
                    IAtomContainer mol = readMolecule(mo.getSource());
                    String smi = generator.create(mol);
                    return new MoleculeObject(smi, "smiles");
                } catch (CDKException e) {
                    throw new RuntimeException("Failed to convert structure to format " + toFormat, e);
                }
            });
        } else if (toFormat.equals("mol")) {
            results = input.map(mo -> {
                try {
                    IAtomContainer mol = readMolecule(mo.getSource());
                    MoleculeObject result;
                    if (toFormat.startsWith("mol:v2")) {
                        result = convertToMolfileV2000(mol);
                    } else if (toFormat.startsWith("mol:v3")) {
                        result = convertToMolfileV3000(mol);
                    } else {
                        result = convertToMolfile(mol);
                    }
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to convert structure to format " + toFormat, e);
                }
            });
        } else {
            throw new IllegalArgumentException("Unsupported format: " + toFormat);
        }

        return results;
    }

    public static MoleculeObject convertToFormat(IAtomContainer mol, String format) throws IOException, CDKException {
        if (format.equals("smiles")) {
            return convertToSmiles(mol);
        } else if (format.equals("mol") || format.startsWith("mol:")) {
            if (format.startsWith("mol:v2")) {
                return convertToMolfileV2000(mol);
            } else if (format.startsWith("mol:v3")) {
                return convertToMolfileV3000(mol);
            } else {
                return convertToMolfile(mol);
            }
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    public static MoleculeObject convertToSmiles(IAtomContainer mol) throws CDKException {
        SmilesGenerator generator = new SmilesGenerator(SmiFlavor.Absolute);
        String smi = generator.create(mol);
        return new MoleculeObject(smi, "smiles");
    }

    public static MoleculeObject convertToSmiles(IAtomContainer mol, SmilesGenerator generator) throws CDKException {
        String smi = generator.create(mol);
        return new MoleculeObject(smi, "smiles");
    }

    public static MoleculeObject convertToMolfile(IAtomContainer mol) throws IOException, CDKException {
        try {
            return convertToMolfileV2000(mol);
        } catch (Exception e) {
            return convertToMolfileV3000(mol);
        }
    }

    public static MoleculeObject convertToMolfileV2000(IAtomContainer mol) throws IOException, CDKException {
        StringWriter writer = new StringWriter();
        try (MDLV2000Writer mdl = createMDLV2000Writer(writer)) {
            mdl.write(mol);
        }
        String source = writer.toString();
        return new MoleculeObject(source, "mol:v2");
    }

    public static MoleculeObject convertToMolfileV3000(IAtomContainer mol) throws IOException, CDKException {
        StringWriter writer = new StringWriter();
        try (MDLV3000Writer mdl = createMDLV3000Writer(writer)) {
            mdl.write(mol);
        }
        String source = writer.toString();
        return new MoleculeObject(source, "mol:v3");
    }


    public static MoleculeObject convertMolecule(IAtomContainer mol, DefaultChemObjectWriter objectWriter, String formatString) throws IOException, CDKException {
        StringWriter writer = new StringWriter();
        objectWriter.setWriter(writer);
        objectWriter.write(mol);
        writer.close();
        String source = writer.toString();
        return new MoleculeObject(source, formatString);
    }

}
