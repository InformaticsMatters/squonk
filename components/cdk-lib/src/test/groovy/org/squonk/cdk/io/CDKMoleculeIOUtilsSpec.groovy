package org.squonk.cdk.io

import java.util.zip.GZIPInputStream
import spock.lang.Specification

import org.openscience.cdk.io.*;

/**
 *
 * @author timbo
 */
class CDKMoleculeIOUtilsSpec extends Specification {
	
    
    void "molecule iterable for smiles"() {
        
        String smiles = '''CC1=CC(=O)C=CC1=O	1
S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4	2
OC1=C(Cl)C=C(C=C1[N+]([O-])=O)[N+]([O-])=O	3
[O-][N+](=O)C1=CNC(=N)S1	4
NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O	5'''
        
        when:
        def iter = CDKMoleculeIOUtils.moleculeIterable(new ByteArrayInputStream(smiles.getBytes()))
        
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
    
    void "molecule format"() {
         
        expect:
        CDKMoleculeIOUtils.createReader(source).getClass() == reader

        where:
        source << [
'C1=CC=CC=C1',
        '''
  Mrv0541 02231512112D          

  3  2  0  0  0  0            999 V2000
   -4.1987    0.0884    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -3.4842    0.5009    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -2.9008   -0.0825    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0
  1  2  1  0  0  0  0
  2  3  1  0  0  0  0
M  END
''',
            '''InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3'''
        ]
        reader << [
            SMILESReader.class,
            MDLV2000Reader.class,
            INChIReader.class
        ]
        
         
    }
    
}

