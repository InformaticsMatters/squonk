package com.im.lac.camel.chemaxon.processor

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule
import com.im.lac.chemaxon.molecule.ChemTermsEvaluator
import com.im.lac.types.MoleculeObject
import com.im.lac.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class ChemAxonMoleculeProcessorSpec extends CamelSpecificationBase {


    def 'ChemTerms processor for List<Molecule>'() {

        given:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mols = []
        mols << new MoleculeObject('C')
        mols << new MoleculeObject('CC')        
        mols << new MoleculeObject('CCC')
        template.sendBody('direct:static', mols)

        then:
        resultEndpoint.assertIsSatisfied()
        Iterator iter = resultEndpoint.receivedExchanges.in.body[0]
        def list = iter.collect()
        list[0].getValue('atom_count') == 5
        list[1].getValue('atom_count') == 8
        list[2].getValue('atom_count') == 11
        list[0].getValue('bond_count') == 4
        list[1].getValue('bond_count') == 7
        list[2].getValue('bond_count') == 10
    }
    
    def 'ChemTerms processor for Molecule'() {

        given:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(2)
        

        when:
        def mol0 = new MoleculeObject('C')
        def mol1 = new MoleculeObject('CC')  
        template.sendBody('direct:static', mol0)
        template.sendBody('direct:static', mol1)
        

        then:
        resultEndpoint.assertIsSatisfied()
        def result0 = resultEndpoint.receivedExchanges.in.body[0]
        def result1 = resultEndpoint.receivedExchanges.in.body[1]
        result0.getValue('atom_count') == 5
        result1.getValue('atom_count') == 8
        
    }
    
    def 'Dynamic ChemTerms processor for Molecule'() {

        given:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mol0 = new MoleculeObject('C') 
        template.sendBodyAndHeader('direct:dynamic', mol0, 
            ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION, 'atom_count=atomCount();bond_count=bondCount()')
        

        then:
        resultEndpoint.assertIsSatisfied()
        def result0 = resultEndpoint.receivedExchanges.in.body[0]
        println "props: " + result0.getValues()
        result0.getValue('atom_count') == 5
        result0.getValue('bond_count') == 4

    }
    
    def 'conformer for Molecule'() {

        given:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mol0 = new MoleculeObject('OC[C@H]1OC(O)[C@H](O)[C@@H](O)[C@@H]1O')
        mol0.putValue('foo', 'bar')
        template.sendBodyAndHeader('direct:dynamic', mol0, 
            ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION, 'transform=leconformer();energy=mmff94Energy')
        
        then:
        resultEndpoint.assertIsSatisfied()
        def result0 = resultEndpoint.receivedExchanges.in.body[0]
        result0.getRepresentation(Molecule.class.getName()).dim == 3
        result0.getValue('foo') == 'bar'
        result0.getValue('energy') != null
    }
    
    def "simple query param parsing"() {
        
        when:
        def q = ChemAxonMoleculeProcessor.parseParamString("logp=logP();atom_count=atomCount()");
        
        then:
        q.size() == 2
        q[0].propName == 'logp'
        q[0].chemTermsFunction == 'logP()'
        q[1].propName == 'atom_count'
        q[1].chemTermsFunction == 'atomCount()'
        
    }
    
    def "query param with args parsing"() {
        
        when:
        def q = ChemAxonMoleculeProcessor.parseParamString("logd=logD('7.4');atom_count=atomCount()");
        
        then:
        q.size() == 2
        q[0].propName == 'logd'
        q[0].chemTermsFunction == "logD('7.4')"
        q[1].propName == 'atom_count'
        q[1].chemTermsFunction == 'atomCount()'
        
    }
    
    def "query param with filter parsing"() {
        
        when:
        def q = ChemAxonMoleculeProcessor.parseParamString("filter=atomCount()<6;filter=bondCount()<6;logp=logP()");
        
        
        then:
        q.size() == 3
        
        q[0].propName == 'filter'
        q[0].chemTermsFunction == "atomCount()<6"
        q[0].mode == ChemTermsEvaluator.Mode.Filter
        
        q[1].propName == 'filter'
        q[1].chemTermsFunction == "bondCount()<6"
        q[1].mode == ChemTermsEvaluator.Mode.Filter
        
        q[2].propName == 'logp'
        q[2].chemTermsFunction == 'logP()'
        q[2].mode == ChemTermsEvaluator.Mode.Calculate
        
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:static")
                .process(new ChemAxonMoleculeProcessor()
                    .calculate('atom_count', 'atomCount()')
                    .calculate('bond_count', 'bondCount()'))
                .to('mock:result')
                
                from("direct:dynamic")
                .process(new ChemAxonMoleculeProcessor())
                .to('mock:result')
            }
        }
    }
}
