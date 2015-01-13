package com.im.lac.cdk.io

import java.util.zip.GZIPInputStream
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeIOUtilsSpec extends Specification {
	
    
    void "molecule iterable for smiles"() {
        
        String smiles = '''CC1=CC(=O)C=CC1=O	1
S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4	2
OC1=C(Cl)C=C(C=C1[N+]([O-])=O)[N+]([O-])=O	3
[O-][N+](=O)C1=CNC(=N)S1	4
NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O	5'''
        
        when:
        def iter = MoleculeIOUtils.moleculeIterable(new ByteArrayInputStream(smiles.getBytes()))
        
        then:
        iter != null
        iter.iterator().collect().size() == 5
    }
    
    
    void "molecule iterable for sdf"() {
        
        String file = '../../data/testfiles/dhfr_standardized.sdf.gz'
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file))
        
        when:
        def iter = MoleculeIOUtils.moleculeIterable(gzip)
        
        then:
        iter != null
        iter.iterator().collect().size() == 756
    }
    
    
}

