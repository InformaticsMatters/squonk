package com.im.lac.demo.routes

import chemaxon.struc.Molecule
import chemaxon.struc.MolBond
import org.squonk.camel.chemaxon.processor.ChemAxonMoleculeProcessor
import org.squonk.camel.testsupport.CamelSpecificationBase
import com.im.lac.types.MoleculeObject
import org.squonk.dataset.MoleculeObjectDataset
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class CalculatorsRoutesSpec extends CamelSpecificationBase {

    def 'logp single as MoleculeObject'() {

        when:
        def mol = new MoleculeObject('c1ccccc1')
        def result = template.requestBody('direct:logp', mol)

        then:
        result instanceof MoleculeObject
        result.getValue('CXN_LogP') != null
        result.getValue('CXN_LogP') instanceof Number
    }
    
    def 'logp multiple as String'() {

        when:
        def result = template.requestBody('direct:logp', 'c1ccccc1')

        then:
        result instanceof MoleculeObject
        result.getValue('CXN_LogP') != null
        result.getValue('CXN_LogP') instanceof Number
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
        results instanceof MoleculeObjectDataset
        results.items.size() == 3
    }
    
    def 'logp multiple as stream'() {
        def mols = 'C\nCC\nCCC'
        
        when:
        def results = template.requestBody('direct:logp', new ByteArrayInputStream(mols.getBytes()))

        then:
        results.items.size() == 3
    }
    
    def 'logp as stream'() {
        
        when:
        def results = template.requestBody('direct:logp', new FileInputStream("../../data/testfiles/nci100.smiles"))

        then:
        results.items.size() == 100
    }
    
    def 'filter as stream'() {
        
        when:
        def results = template.requestBody('direct:filter_example', new FileInputStream("../../data/testfiles/nci100.smiles"))
                
        then:
        results.items.size() < 100
       
    }
    
    def 'filter dynamic'() {
        
        when:
        long t0 = System.currentTimeMillis()
        def results = template.requestBodyAndHeader(
            'direct:chemTerms', new FileInputStream("../../data/testfiles/nci100.smiles"),
            ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION, "filter=mass()<250"
        )
        long t1 = System.currentTimeMillis()
        long size = results.items.size()
        long t2 = System.currentTimeMillis()
        //println "dynamic filter down to $size first in ${t1-t0}ms last in ${t2-t0}ms"
        then:
        size < 100
        size > 0
       
    }
    
    def 'filter as stream concurrent'() {
        println "filter as stream concurrent()"
        
        when:
        def results = []
        (1..10).each {
            // asyncRequestBody() returns a Future
            results << template.asyncRequestBody('direct:filter_example', new FileInputStream("../../data/testfiles/nci100.smiles"))
        }
        
        then:
        results.size() == 10
        int s0 = results[0].get().items.size()
        (1..9).each {
            def result = results[it].get()
            long count = result.getStream().count()
            //println "result $it count is $count"
            assert count == s0
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
