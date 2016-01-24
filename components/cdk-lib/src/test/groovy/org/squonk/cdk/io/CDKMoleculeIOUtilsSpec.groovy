package org.squonk.cdk.io

import org.openscience.cdk.ChemFile
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.io.formats.IChemFormat
import org.openscience.cdk.silent.AtomContainer
import org.openscience.cdk.tools.manipulator.ChemFileManipulator
import org.squonk.data.Molecules

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

    
}

