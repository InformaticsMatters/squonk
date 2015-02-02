package com.im.lac.chemaxon.clustering

import com.im.lac.chemaxon.molecule.MoleculeIterable
import com.im.lac.chemaxon.molecule.MoleculeUtils
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class SphereExclusionClustererSpec extends Specification {
	
    
    void "test simple"() {
        setup:
        InputStream is = new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz")
        MoleculeIterable iterable = MoleculeUtils.moleculeIterable(is)
        SphereExclusionClusterer clusterer = new SphereExclusionClusterer()
        
        when:
        MoleculeIterable results = clusterer.cluster(iterable)
        def mols = results.collect()
        
        then:
        
        mols.size() == 756
        int max = 0
        mols.each {
           Integer cluster = it.getPropertyObject('cluster')
           cluster != null
           if (cluster > max) { max = cluster }
        }
        max > 0
    }
}

