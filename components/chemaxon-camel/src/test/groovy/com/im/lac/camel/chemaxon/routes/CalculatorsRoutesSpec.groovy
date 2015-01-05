package com.im.lac.camel.chemaxon.routes

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.struc.Molecule
import com.im.lac.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class CalculatorsRoutesSpec extends CamelSpecificationBase {

    def 'logp single as Molecule'() {

        when:
        def mol = MolImporter.importMol('c1ccccc1')
        def result = template.requestBody('direct:logp', mol)

        then:
        result instanceof Molecule
        result.getPropertyObject('logp') != null
        result.getPropertyObject('logp') instanceof Number
    }
    
    def 'logp multiple as String'() {

        when:
        def results = template.requestBody('direct:logp', 'c1ccccc1')

        then:
        results instanceof Iterator
        def result = results.next()
        result.getPropertyObject('logp') != null
        result.getPropertyObject('logp') instanceof Number
    }
    
    def 'logp single as String'() {

        when:
        def result = template.requestBody('direct:logpSingleMolecule', 'c1ccccc1')

        then:
        result instanceof Molecule
        result.getPropertyObject('logp') != null
        result.getPropertyObject('logp') instanceof Number
    }
    
    def 'logp multiple as Molecules'() {
        def mols = []
        mols << MolImporter.importMol('C')
        mols << MolImporter.importMol('CC')        
        mols << MolImporter.importMol('CCC')
        
        when:
        def results = template.requestBody('direct:logp', mols)

        then:
        results instanceof Iterator
        results.collect().size() == 3
    }
    
    def 'logp multiple as stream'() {
        def mols = 'C\nCC\nCCC'
        
        when:
        def results = template.requestBody('direct:logp', new ByteArrayInputStream(mols.getBytes()))

        then:
        results.collect().size() == 3
    }
    
    def 'logp as stream'() {
        
        when:
        def results = template.requestBody('direct:logp', new FileInputStream("../../data/testfiles/nci1000.smiles"))

        then:
        results.collect().size() >100
    }
    
    def 'filter as stream'() {
        
        when:
        long t0 = System.currentTimeMillis()
        def results = template.requestBody('direct:filter_example', new FileInputStream("../../data/testfiles/nci1000.smiles"))
        long t1 = System.currentTimeMillis()
        int size = results.collect().size()
        long t2 = System.currentTimeMillis()
        println "filter down to $size first in ${t1-t0}ms last in ${t2-t0}ms"
        then:
        size < 1000
    }
    
    def 'multiple props'() {

        when:
        def mol = MolImporter.importMol('c1ccccc1')
        def result = template.requestBody('direct:logp_atomcount_bondcount', mol)

        then:
        result instanceof Molecule
        result.getPropertyObject('logp') != null
        result.getPropertyObject('atom_count') != null
        result.getPropertyObject('bond_count') != null
    } 

    @Override
    RouteBuilder createRouteBuilder() {
        return new CalculatorsRouteBuilder()
    }
}
