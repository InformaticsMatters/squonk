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

package org.squonk.cdk.io

import org.openscience.cdk.ChemFile
import org.openscience.cdk.DefaultChemObjectBuilder
import org.openscience.cdk.fingerprint.SignatureFingerprinter
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IPDBPolymer
import org.openscience.cdk.io.*
import org.openscience.cdk.io.formats.IChemFormat
import org.openscience.cdk.signature.MoleculeSignature
import org.openscience.cdk.silent.AtomContainer
import org.openscience.cdk.silent.SilentChemObjectBuilder
import org.openscience.cdk.smiles.SmiFlavor
import org.openscience.cdk.smiles.SmilesGenerator
import org.openscience.cdk.smiles.SmilesParser
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.openscience.cdk.tools.manipulator.ChemFileManipulator
import org.squonk.data.Molecules
import org.squonk.types.CDKSDFile
import org.squonk.types.MoleculeObject
import org.squonk.util.IOUtils
import spock.lang.Specification

import java.util.zip.GZIPInputStream

/**
 *
 * @author timbo
 */
class CDKMoleculeIOUtilsSpec extends Specification {
	
    
    void "molecule iterable for smiles"() {
        
        String smiles5 = '''CC1=CC(=O)C=CC1=O	1
S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4	2
OC1=C(Cl)C=C(C=C1[N+]([O-])=O)[N+]([O-])=O	3
[O-][N+](=O)C1=CNC(=N)S1	4
NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O	5'''
        
        when:
        def iter = CDKMoleculeIOUtils.moleculeIterable(new ByteArrayInputStream(smiles5.getBytes()))
        
        then:
        iter != null
        iter.iterator().collect().size() == 5
    }
    
    
    void "molecule iterable for sdf"() {
        
        String file = '../../data/testfiles/dhfr_standardized.sdf.gz'
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file))
        
        when:
        def iter = CDKMoleculeIOUtils.moleculeIterable(gzip)
        
        then:
        iter != null
        iter.iterator().collect().size() == 756
    }
    
    void "read molecule guess format"() {
         
        expect:
        IAtomContainer mol = CDKMoleculeIOUtils.readMolecule(source)
        mol.getAtomCount() > 0

        where:
        source << [Molecules.ethanol.smiles, Molecules.ethanol.v2000, Molecules.ethanol.v3000]
    }

    void "read molecule with format"() {

        expect:
        CDKMoleculeIOUtils.readMolecule(source, format) instanceof IAtomContainer

        where:
        source | format
        Molecules.ethanol.smiles | "smiles"
        Molecules.ethanol.v2000  | "mol:v2"
        Molecules.ethanol.v3000  | "mol:v3"
        Molecules.ethanol.v2000  | "mol"
        Molecules.ethanol.v3000  | "mol"
    }

    void "cdk convert molecule formats"() {

        SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance())

        expect:
        def mol = smilesParser.parseSmiles(Molecules.ethanol.smiles)
        String m = CDKMoleculeIOUtils.convertToFormat(mol, format)
        m.length() > 0
        m.contains(result)

        where:
        format   | result
        "mol"    | "V2000"
        "mol:v2" | "V2000"
        "mol:v3" | "V3000"
        "smiles" | "CCO"
    }

    void "test read multiple v2000"() {

        when:
        IChemFormat format = new FormatFactory().guessFormat(new ByteArrayInputStream(Molecules.ethanol.v2000.getBytes()));
        ISimpleChemObjectReader reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
        reader.setReader(new ByteArrayInputStream(Molecules.ethanol.v2000.getBytes()));
        ChemFile chemFile = reader.read(new ChemFile());
        List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);

        then:
        containersList != null
        containersList.size() == 1
    }

//    void "test read multiple v3000"() {
//
//        when:
//        IChemFormat format = new FormatFactory().guessFormat(new ByteArrayInputStream(v3000.getBytes()));
//        ISimpleChemObjectReader reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
//        reader.setReader(new ByteArrayInputStream(v3000.getBytes()));
//        ChemFile chemFile = reader.read(new ChemFile());
//        List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
//
//        then:
//        containersList != null
//        containersList.size() == 1
//    }

    void "test read single v3000"() {

        when:
        IChemFormat format = new FormatFactory().guessFormat(new ByteArrayInputStream(Molecules.ethanol.v3000.getBytes()));
        ISimpleChemObjectReader reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
        reader.setReader(new ByteArrayInputStream(Molecules.ethanol.v3000.getBytes()));
        IAtomContainer mol = reader.read(new AtomContainer());

        then:
        mol != null
    }

    void "reader direct single v2000"() {

        when:
        ISimpleChemObjectReader reader = new MDLV2000Reader(new ByteArrayInputStream(Molecules.ethanol.v2000.getBytes()))
        IAtomContainer mol = reader.read(new AtomContainer())

        then:
        mol != null
    }

    void "reader direct single v3000"() {

        when:
        ISimpleChemObjectReader reader = new MDLV3000Reader(new ByteArrayInputStream(Molecules.ethanol.v3000.getBytes()))
        IAtomContainer mol = reader.read(new AtomContainer())

        then:
        mol != null
    }

    void "reader direct multiple v2000"() {

        when:
        ISimpleChemObjectReader reader = new MDLV2000Reader(new ByteArrayInputStream(Molecules.ethanol.v2000.getBytes()))
        ChemFile chemFile = reader.read(new ChemFile());
        List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);

        then:
        containersList != null
        containersList.size() == 1
    }


//    void "reader direct multiple v3000"() {
//
//        when:
//        ISimpleChemObjectReader reader = new MDLV3000Reader(new ByteArrayInputStream(v3000.getBytes()))
//        ChemFile chemFile = reader.read(new ChemFile());
//        List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
//
//        then:
//        containersList != null
//        containersList.size() == 1
//    }


    void "write sdf"() {

        def mols = [
                new MoleculeObject('CC1=CC(=O)C=CC1=O', 'smiles', [fruit: 'apple', index: 1]),
                new MoleculeObject('S(SC1=NC2=CC=CC=C2S1)', 'smiles', [fruit: 'orange', index: 2]),
                new MoleculeObject('CC(=O)OC(CC([O-])=O)C[N+](C)(C)C', 'smiles', [fruit: 'pear', index: 3]),
                new MoleculeObject('[O-][N+](=O)C1=CC(=C(Cl)C=C1)[N+]([O-])=O', 'smiles', [fruit: 'banana', index: 4]),
                new MoleculeObject('OC1C(O)C(O)C(OP(O)(O)=O)C(O)C1O', 'smiles', [fruit: 'melon', index: 5])
        ]

        when:
        CDKSDFile sdf = CDKMoleculeIOUtils.covertToSDFile(mols.stream(), true)
        String content = IOUtils.convertStreamToString(sdf.inputStream)
        //println content

        then:
        content.length() > 0
        content.split('fruit').length == 6
    }

    void "write sdf handle invalid continue"() {

        def mols = [
                new MoleculeObject('CC1=CC(=O)C=CC1=O', 'smiles', [fruit: 'apple', index: 1]),
                new MoleculeObject('S(SC1=NC2=CC=CC=C2S1)', 'smiles', [fruit: 'orange', index: 2]),
                new MoleculeObject('CC(=OZZZZZZZZZZZ(C)(C)C', 'smiles', [fruit: 'pear', index: 3]),
                new MoleculeObject('[O-][N+](=O)C1=CC(=C(Cl)C=C1)[N+]([O-])=O', 'smiles', [fruit: 'banana', index: 4]),
                new MoleculeObject('OC1C(O)C(O)C(OP(O)(O)=O)C(O)C1O', 'smiles', [fruit: 'melon', index: 5])
        ]

        when:
        CDKSDFile sdf = CDKMoleculeIOUtils.covertToSDFile(mols.stream(), false)
        String content = IOUtils.convertStreamToString(sdf.inputStream)
        //println content

        then:
        content.length() > 0
        content.split('fruit').length == 6
    }

    void "write sdf handle invalid fail"() {

        // this doesn't work very well.
        // rather than throwing exception the results are truncated
        // this is because the stream procecssing is being done in a different thread.

        def mols = [
                new MoleculeObject('CC1=CC(=O)C=CC1=O', 'smiles', [fruit: 'apple', index: 1]),
                new MoleculeObject('S(SC1=NC2=CC=CC=C2S1)', 'smiles', [fruit: 'orange', index: 2]),
                new MoleculeObject('CC(=OZZZZZZZZZZZ(C)(C)C', 'smiles', [fruit: 'pear', index: 3]),
                new MoleculeObject('[O-][N+](=O)C1=CC(=C(Cl)C=C1)[N+]([O-])=O', 'smiles', [fruit: 'banana', index: 4]),
                new MoleculeObject('OC1C(O)C(O)C(OP(O)(O)=O)C(O)C1O', 'smiles', [fruit: 'melon', index: 5])
        ]

        when:
        CDKSDFile sdf = CDKMoleculeIOUtils.covertToSDFile(mols.stream(), true)
        String content = IOUtils.convertStreamToString(sdf.inputStream)
        //println content

        then:
        content.length() > 0
        content.split('fruit').length == 3 // ideally this would throw exception.
    }

    void "write sdf kinase"() {

        def dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)

        when:
        CDKSDFile sdf = CDKMoleculeIOUtils.covertToSDFile(dataset.getStream(), true)
        String content = IOUtils.convertStreamToString(sdf.inputStream)
        //println content

        then:
        content.length() > 0
        content.split('<mr_id>').length == 37
    }


    void "write csv kinase"() {

        String file = '../../data/testfiles/dhfr_standardized.sdf.gz'
        GZIPInputStream input = new GZIPInputStream(new FileInputStream(file))

        when:
        Iterable<IAtomContainer> mols = CDKMoleculeIOUtils.moleculeIterable(input)
        Iterator iter = mols.iterator()

        def propnames = [] as HashSet
        while (iter.hasNext()) {
            def mol = iter.next()
            def props = mol.getProperties()
            props.each { k,v ->
                if (!propnames.contains(k) && !k.startsWith("cdk:")) {
                    propnames.add(k)
                }
            }
         }

        iter = mols.iterator()
        SmilesGenerator generator = new SmilesGenerator(SmiFlavor.Absolute);

        //print "smiles"
        propnames.each { n ->
            //print "," + n
        }
        //println ""
        while (iter.hasNext()) {
            def mol = iter.next()
            String smi = generator.create(mol);
            //print smi

            def props = mol.getProperties()
            propnames.each { n ->
                //print "," + props[n]
            }
            //println ""
        }


        then:
        1 == 1

        cleanup:
        input?.close()

    }


    void "read pdb write mol2"() {
        InputStream is = new GZIPInputStream(new FileInputStream("../../data/testfiles/1cx2.pdb.gz"))
        PDBReader reader = new PDBReader(is);
        ChemFile file = reader.read(new ChemFile());
        IPDBPolymer structure = (IPDBPolymer)ChemFileManipulator
                .getAllAtomContainers(file).get(0);

        //println structure.getClass().name

//        structure.structures.each {
//            println "${it.getClass().name} ${it.structureType} ${it.startChainID} ${it.endChainID}"
//        }

//        structure.strands.each { k,v ->
//            println "$k $v.strandType $v.monomerCount"
//            v.monomers.each { n, m ->
//                println "  $m.monomerName $m.monomerType $m.atoms.length"
//            }
//        }

        def out = new ByteArrayOutputStream()


        when:
        Mol2Writer writer = new Mol2Writer(out)
        writer.write(structure)
        writer.close()

        then:
        String result = new String(out.toByteArray())
        //println result
        result.length() > 0

        cleanup:
        is.close()
    }

    void "read smiles write mol2"() {
        SmilesParser   sp  = new SmilesParser(SilentChemObjectBuilder.getInstance());
        IAtomContainer m   = sp.parseSmiles("c1ccccc1");

        def out = new ByteArrayOutputStream()


        when:
        Mol2Writer writer = new Mol2Writer(out)
        writer.write(m)
        writer.close()

        then:
        String result = new String(out.toByteArray())
        //println result
        result.length() > 0

    }

    void "read mol2 protein"() {
        InputStream is = new GZIPInputStream(new FileInputStream("../../data/testfiles/protein.mol2.gz"))
        Mol2Reader reader = new Mol2Reader(is);


        when:
        ChemFile file = reader.read(new ChemFile());
        def mols = ChemFileManipulator.getAllAtomContainers(file)


        then:
        mols.size() == 1
        //println mols[0].getClass().name

    }


    void "read mol2 ligand"() {
        InputStream is = new FileInputStream("../../data/testfiles/ligand.mol2")
        Mol2Reader reader = new Mol2Reader(is)

        when:
        def file = reader.read(new ChemFile())
        def mol = ChemFileManipulator.getAllAtomContainers(file).get(0)
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol)
        CDKHydrogenAdder.getInstance(mol.getBuilder()).addImplicitHydrogens(mol)
        mol.atoms().each {
            it.implicitHydrogenCount = 0
        }
        def out = new ByteArrayOutputStream()
        def writer = new SMILESWriter(out)
        writer.writeAtomContainer(mol)
        def result = new String(out.toByteArray())

        then:
        //println result
        result.length() > 0

    }


    void "signatures for sdf"() {

        //String file = '../../data/testfiles/Kinase_inhibs.sdf.gz'
        String file = "../../data/testfiles/dhfr_standardized.sdf.gz"
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file))

        when:
        def iter = CDKMoleculeIOUtils.moleculeIterable(gzip)
        SignatureFingerprinter fingerprinter = new SignatureFingerprinter(3)
        def soFar = [] as HashSet
        int i = 0
        def sigs = iter.collect { m ->
            MoleculeSignature moleculeSignature = new MoleculeSignature(m);
            String canonicalSignature = moleculeSignature.toCanonicalString();
            //println "================================================"
            //println canonicalSignature
            def fp = fingerprinter.getRawFingerprint(m)
            //println "------------------------------------------------"
            fp.each { k,v ->
                soFar << k
                //println "$v $k"
            }
            //println "$i ${soFar.size()}"
            i++
            return canonicalSignature
        }

        then:
        sigs.size() == 756
    }

    /** Tests that CDK has not regressed wrt handling the title line
     *
     */
    void "title line"() {

        SmilesParser parser = new SmilesParser(SilentChemObjectBuilder.getInstance())
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        SDFWriter sdf = new SDFWriter(out)
        sdf.write(parser.parseSmiles(smiles))
        sdf.close()
        def txt = new String(out.toByteArray())

        expect:
        txt.contains("ethanol") == result
        txt.contains("cdk:Title") == false

        where:
        smiles        | result
        "CCO"         | false
        "CCO ethanol" | true
    }

}

