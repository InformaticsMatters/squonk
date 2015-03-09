package com.im.lac.util

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class CollectionUtilsSpec extends Specification {

    
    void "enumeration"() {
        setup:
        List<String> mols1 = [new String("A"),new String("B"),new String("C"),new String("D")]
        List<String> mols2 = [new String("1"),new String("2"),new String("3")]
        List<String> mols3 = [new String("X"),new String("Y"),new String("Z")]
        
        
        
        when:
        def q = CollectionUtils.combinatorialIterator(100, mols1, mols2, mols3)
        def all = q.collect()
        
        then:
        all.size() == 36
        
    }
    
    
}

