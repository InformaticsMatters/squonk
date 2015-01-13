package com.im.lac.cdk.molecule

import java.util.zip.GZIPInputStream
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.smiles.SmilesParser
import org.openscience.cdk.DefaultChemObjectBuilder
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MolecularDescriptorsSpec extends Specification {
	
    
    void "wiener index"() {
        
        setup:
        String smiles = 'S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4'
        SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer mol = smilesParser.parseSmiles(smiles);

        when:
        def result = MoleculeDescriptors.wienerNumbers(mol)
        
        then:
        result.getProperty(MoleculeDescriptors.WIENER_PATH) != null
        result.getProperty(MoleculeDescriptors.WIENER_POLARITY) != null
    }
    
    void "alogp"() {
        
        setup:
        String smiles = 'S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4'
        SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer mol = smilesParser.parseSmiles(smiles);

        when:
        def result = MoleculeDescriptors.aLogP(mol)
        
        then:
        result != null
        result.getProperty(MoleculeDescriptors.ALOGP_ALOPG) != null
        result.getProperty(MoleculeDescriptors.ALOGP_ALOPG2) != null
        result.getProperty(MoleculeDescriptors.ALOGP_AMR) != null

    }
    
    void "hbond acceptor count"() {
        
        setup:
        String smiles = 'S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4'
        SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer mol = smilesParser.parseSmiles(smiles);

        when:
        def result = MoleculeDescriptors.hbondAcceptorCount(mol)
        
        then:
        result != null
        result.getProperty(MoleculeDescriptors.HBOND_ACCEPTOR_COUNT) != null

    }
    void "hbond donor count"() {
        
        setup:
        String smiles = 'S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4'
        SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer mol = smilesParser.parseSmiles(smiles);

        when:
        def result = MoleculeDescriptors.hbondDonorCount(mol)
        
        then:
        result != null
        result.getProperty(MoleculeDescriptors.HBOND_DONOR_COUNT) != null

    }
    
}

