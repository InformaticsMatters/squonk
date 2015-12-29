package org.squonk.cdk.molecule

import com.im.lac.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MolecularDescriptorsSpec extends Specification {
	
    
    void "wiener index"() {
        
        setup:
        MoleculeObject mo = new MoleculeObject('S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4')

        when:
        MolecularDescriptors.wienerNumbers(mo)
        
        then:
        mo.getValue(MolecularDescriptors.WIENER_PATH) != null
        mo.getValue(MolecularDescriptors.WIENER_POLARITY) != null
    }
    
    void "alogp"() {
        
        setup:
        MoleculeObject mo = new MoleculeObject('S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4')

        when:
        MolecularDescriptors.aLogP(mo)
        
        then:
        mo.getValue(MolecularDescriptors.ALOGP_ALOPG) != null
        mo.getValue(MolecularDescriptors.ALOGP_ALOPG2) != null
        mo.getValue(MolecularDescriptors.ALOGP_AMR) != null
    }
    
    void "xlogp"() {
        
        setup:
        MoleculeObject mo = new MoleculeObject('S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4')

        when:
        MolecularDescriptors.xLogP(mo)
        
        then:
        mo.getValue(MolecularDescriptors.XLOGP_XLOGP) != null
    }
    
    void "hbond acceptor count"() {
        
        setup:
        MoleculeObject mo = new MoleculeObject('S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4')

        when:
        MolecularDescriptors.hbondAcceptorCount(mo)
        
        then:
        mo.getValue(MolecularDescriptors.HBOND_ACCEPTOR_COUNT) != null
    }
    
    void "hbond donor count"() {
        
        setup:
        MoleculeObject mo = new MoleculeObject('S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4')

        when:
        MolecularDescriptors.hbondDonorCount(mo)
        
        then:
        mo.getValue(MolecularDescriptors.HBOND_DONOR_COUNT) != null
    }
    
}

