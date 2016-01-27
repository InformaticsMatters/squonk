package org.squonk.cdk.io

import org.openscience.cdk.ChemFile
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.io.formats.IChemFormat
import org.openscience.cdk.silent.AtomContainer
import org.openscience.cdk.tools.manipulator.ChemFileManipulator

import java.util.zip.GZIPInputStream
import spock.lang.Specification

import org.openscience.cdk.io.*;

/**
 *
 * @author timbo
 */
class CDKMoleculeIOUtilsSpec extends Specification {

    static String v2000 = '''
  Mrv0541 02231512112D

  3  2  0  0  0  0            999 V2000
   -4.1987    0.0884    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -3.4842    0.5009    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -2.9008   -0.0825    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0
  1  2  1  0  0  0  0
  2  3  1  0  0  0  0
M  END
'''

    static String v3000 = '''
  Mrv0541 01191615492D

  0  0  0     0  0            999 V3000
M  V30 BEGIN CTAB
M  V30 COUNTS 3 2 0 0 0
M  V30 BEGIN ATOM
M  V30 1 C 2.31 -1.3337 0 0
M  V30 2 C 3.6437 -2.1037 0 0
M  V30 3 O 4.9774 -1.3337 0 0
M  V30 END ATOM
M  V30 BEGIN BOND
M  V30 1 1 1 2
M  V30 2 1 2 3
M  V30 END BOND
M  V30 END CTAB
M  END
'''

    static String smiles = 'CCO'
    static String inchi = 'InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3'
	
    
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
        source << [smiles, v2000, v3000]
    }

    void "test read multiple v2000"() {

        when:
        IChemFormat format = new FormatFactory().guessFormat(new ByteArrayInputStream(v2000.getBytes()));
        ISimpleChemObjectReader reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
        reader.setReader(new ByteArrayInputStream(v2000.getBytes()));
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
        IChemFormat format = new FormatFactory().guessFormat(new ByteArrayInputStream(v3000.getBytes()));
        println "format: " + format
        ISimpleChemObjectReader reader = (ISimpleChemObjectReader) (Class.forName(format.getReaderClassName()).newInstance());
        println "reader: " + reader
        reader.setReader(new ByteArrayInputStream(v3000.getBytes()));
        IAtomContainer mol = reader.read(new AtomContainer());

        then:
        mol != null
    }

    void "reader direct single v2000"() {

        when:
        ISimpleChemObjectReader reader = new MDLV2000Reader(new ByteArrayInputStream(v2000.getBytes()))
        IAtomContainer mol = reader.read(new AtomContainer())

        then:
        mol != null
    }

    void "reader direct single v3000"() {

        when:
        ISimpleChemObjectReader reader = new MDLV3000Reader(new ByteArrayInputStream(v3000.getBytes()))
        IAtomContainer mol = reader.read(new AtomContainer())

        then:
        mol != null
    }

    void "reader direct multiple v2000"() {

        when:
        ISimpleChemObjectReader reader = new MDLV2000Reader(new ByteArrayInputStream(v2000.getBytes()))
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

