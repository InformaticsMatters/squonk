package com.im.lac.demo.routes

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.struc.Molecule
import chemaxon.struc.MolBond
import com.im.lac.camel.chemaxon.processor.ChemAxonMoleculeProcessor
import com.im.lac.camel.testsupport.CamelSpecificationBase
import com.im.lac.types.MoleculeObject
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class CalculatorsRoutesSpec extends CamelSpecificationBase {

    def 'logp single as Molecule'() {

        when:
        def mol = new MoleculeObject('c1ccccc1')
        def result = template.requestBody('direct:logp', mol)

        then:
        result instanceof MoleculeObject
        result.getValue('logp') != null
        result.getValue('logp') instanceof Number
    }
    
    def 'logp multiple as String'() {

        when:
        def results = template.requestBody('direct:logp', 'c1ccccc1')

        then:
        results instanceof Iterator
        def result = results.next()
        result.getValue('logp') != null
        result.getValue('logp') instanceof Number
    }
    
    def 'logp single as String'() {

        when:
        def result = template.requestBody('direct:logpSingleMolecule', 'c1ccccc1')

        then:
        result instanceof MoleculeObject
        result.getValue('logp') != null
        result.getValue('logp') instanceof Number
    }
    
    def 'logp multiple as Molecules'() {
        def mols = []
        mols << new MoleculeObject('C')
        mols << new MoleculeObject('CC')        
        mols << new MoleculeObject('CCC')
        
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
        def results = template.requestBody('direct:filter_example', new FileInputStream("../../data/testfiles/nci1000.smiles"))
        int size = results.collect().size()
        
        then:
        size < 1000
       
    }
    
    def 'filter dynamic'() {
        
        when:
        long t0 = System.currentTimeMillis()
        def results = template.requestBodyAndHeader(
            'direct:chemTerms', new FileInputStream("../../data/testfiles/nci1000.smiles"),
            ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION, "filter=mass()<250"
        )
        long t1 = System.currentTimeMillis()
        int size = results.collect().size()
        long t2 = System.currentTimeMillis()
        //println "dynamic filter down to $size first in ${t1-t0}ms last in ${t2-t0}ms"
        then:
        size < 1000
        size > 0
       
    }
    
    def 'filter as stream concurrent'() {
        
        when:
        def results = []
        (1..10).each {
            results << template.requestBody('direct:filter_example', new FileInputStream("../../data/testfiles/nci1000.smiles"))
        }
        int size = results.size()
        then:
        size == 10
        int s0 = results[0].collect().size()
        (2..10).each {
            results[it].collect().size() == s0
        }
       
    }
    
    def 'multiple props'() {

        when:
        def mol = new MoleculeObject('c1ccccc1')
        def result = template.requestBody('direct:logp_atomcount_bondcount', mol)

        then:
        result instanceof MoleculeObject
        result.getValue('logp') != null
        result.getValue('atom_count') != null
        result.getValue('bond_count') != null
    } 
    
    def 'standardize molecule'() {

        when:
        def mol = new MoleculeObject('C1=CC=CC=C1')
        def result = template.requestBody('direct:standardize', mol)

        then:
        result instanceof MoleculeObject
        result.getRepresentation(Molecule.class.getName()).getBond(0).getType() == MolBond.AROMATIC
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new CalculatorsRouteBuilder()
    }
}
