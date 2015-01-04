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
    
    def 'logp huge as stream'() {
        
        when:
        def results = template.requestBody('direct:logp', new FileInputStream("../../data/testfiles/nci1000.smiles"))

        then:
        results.collect().size() >100
    }
    
    def 'multiple props'() {

        when:
        def mol = MolImporter.importMol('c1ccccc1')
        def result = template.requestBody('direct:logp_atomcount_bondcount', mol)

        then:
        result instanceof Molecule
        result.getPropertyObject('logp') != null
        result.getPropertyObject('atomCount') != null
        result.getPropertyObject('bondCount') != null
    } 

    @Override
    RouteBuilder createRouteBuilder() {
        return new CalculatorsRouteBuilder()
    }
}
