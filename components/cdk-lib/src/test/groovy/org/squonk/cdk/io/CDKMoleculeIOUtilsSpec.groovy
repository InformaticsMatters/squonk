package org.squonk.cdk.io

import org.squonk.types.MoleculeObject
import org.openscience.cdk.ChemFile
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IPDBPolymer
import org.openscience.cdk.io.formats.IChemFormat
import org.openscience.cdk.silent.AtomContainer
import org.openscience.cdk.silent.SilentChemObjectBuilder
import org.openscience.cdk.smiles.SmilesParser
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.openscience.cdk.tools.manipulator.ChemFileManipulator
import org.squonk.data.Molecules
import org.squonk.types.CDKSDFile
import org.squonk.util.IOUtils

import java.util.zip.GZIPInputStream
import spock.lang.Specification

import org.openscience.cdk.io.*;

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
    
    void "read molecule"() {
         
        expect:
        CDKMoleculeIOUtils.readMolecule(source).getClass() != null

        where:
        source << [Molecules.ethanol.smiles, Molecules.ethanol.v2000, Molecules.ethanol.v3000]
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
        println "format: " + format
        ISimpleChemObjectReader reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
        println "reader: " + reader
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

    void "read pdb write mol2"() {
        InputStream is = new GZIPInputStream(new FileInputStream("../../data/testfiles/1cx2.pdb.gz"))
        PDBReader reader = new PDBReader(is);
        ChemFile file = reader.read(new ChemFile());
        IPDBPolymer structure = (IPDBPolymer)ChemFileManipulator
                .getAllAtomContainers(file).get(0);

        println structure.getClass().name

        structure.structures.each {
            println "${it.getClass().name} ${it.structureType} ${it.startChainID} ${it.endChainID}"
        }

        structure.strands.each { k,v ->
            println "$k $v.strandType $v.monomerCount"
            v.monomers.each { n, m ->
                println "  $m.monomerName $m.monomerType $m.atoms.length"
            }
        }

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
        println result
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
        println mols[0].getClass().name

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
        println result
        result.length() > 0

    }
    
}

