package com.im.lac.chemaxon.molecule

import spock.lang.Specification
import com.im.lac.types.MoleculeObject
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
class SmilesDeduplicatorSpec extends Specification {
    
    private Stream createStream(List mols) {
        MoleculeObject[] molobjs = new MoleculeObject[mols.size()]
        mols.eachWithIndex { m, i ->
            molobjs[i] = new MoleculeObject(m)
        }
        return Stream.of(molobjs)
    }
    
    void "simple test with 0 duplicates"() {

        println "simple test with smiles as input()"
        def input = createStream(['c1ccccc1','c1ccncc1','CCCC','c1ccccc1C'])
        def dedup = new SmilesDeduplicator()
        
        when:
        long count = dedup.processStream(input).count()
        
        then:
        dedup.getTotal() == 4
        dedup.getDuplicateCount() == 0
        count == 4
    }
    
    void "simple test with 1 duplicate"() {

        println "simple test with 1 duplicate()"
        def input = createStream(['c1ccccc1','c1ccncc1','CCCC','c1ccccc1'])
        def dedup = new SmilesDeduplicator()
        
        when :
        long count = dedup.processStream(input).count()
        
        then:
        dedup.getTotal() == 4
        dedup.getDuplicateCount() == 1
        count == 3
    }
    
    void "test unique smiles"() {

        println "test unique smiles()"
        def input = createStream(['c1cnccc1','c1ccncc1','c1ncccc1','c1cccnc1','c1ccccn1'])
        def dedup = new SmilesDeduplicator()
        
        when:
        long count = dedup.processStream(input).count()
        
        then:
        dedup.getTotal() == 5
        dedup.getDuplicateCount() == 4
        count == 1
    }
    
    void "test aromatization"() {

        println "test aromatization()"
        def input = createStream(['c1ccccc1','C1=CC=CC=C1'])
        def dedup = new SmilesDeduplicator()
        
        when:
        long count = dedup.processStream(input).count()
        
        then:
        dedup.getTotal() == 2
        dedup.getDuplicateCount() == 1
        count == 1
    }
    
    void "test explicitH"() {

        println "test explicitH()"
        def input = createStream(['C1=CC=CC=C1[H]','C1=CC=CC=C1'])
        def dedup = new SmilesDeduplicator()
        
        when:
        long count = dedup.processStream(input).count()
        
        then:
        dedup.getTotal() == 2
        dedup.getDuplicateCount() == 1
        count == 1
    }
    
    
    void "test bad smiles exclude"() {

        println "test bad smiles exclude()"
        def input = createStream(['C1=CC=CC=C1','dsf&&%sdf333F'])
        def dedup = new SmilesDeduplicator()
        
        when:
        long count = dedup.processStream(input).count()
        
        then:
        count == 1
        dedup.getTotal() == 2
        dedup.getDuplicateCount() == 0
        dedup.getErrorCount() == 1
    }
    
    void "test bad smiles include"() {

        println "test bad smiles include()"
        def input = createStream(['C1=CC=CC=C1','dsf&&%sdf333F'])
        def dedup = new SmilesDeduplicator(false, true, false)
        
        when:
        long count = dedup.processStream(input).count()
        
        then:
        count == 2
        dedup.getTotal() == 2
        dedup.getDuplicateCount() == 0
        dedup.getErrorCount() == 1
    }
	
}

