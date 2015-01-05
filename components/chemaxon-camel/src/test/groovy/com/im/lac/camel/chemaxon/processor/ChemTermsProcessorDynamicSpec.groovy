package com.im.lac.camel.chemaxon.processor

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule
import com.im.lac.camel.testsupport.CamelSpecificationBase

import java.nio.charset.Charset;

import org.apache.camel.builder.RouteBuilder

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * Created by timbo on 14/04/2014.
 */
class ChemTermsProcessorDynamicSpec extends CamelSpecificationBase {

    
    def 'ChemTerms processor for Molecule'() {

        given:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mol0 = MolImporter.importMol('C') 
        template.sendBodyAndHeader('direct:chemTermsCalculator', mol0, 
            ChemTermsProcessor.PROP_EVALUATORS_DEFINTION, 'atom_Count=atomCount();bond_count=bondCount()')
        

        then:
        resultEndpoint.assertIsSatisfied()
        Molecule result0 = resultEndpoint.receivedExchanges.in.body[0]
        result0.getPropertyObject('atom_count') == 5
        result0.getPropertyObject('bond_count') == 4

        
    }
    
    def "simple query param parsing"() {
        
        when:
        def q = ChemTermsProcessor.parseParamString("logp=logP();atom_count=atomCount()");
        
        then:
        q.size() == 2
        q[0].propName == 'logp'
        q[0].chemTermsFunction == 'logP()'
        q[1].propName == 'atom_count'
        q[1].chemTermsFunction == 'atomCount()'
        
    }
    
    def "query param with args parsing"() {
        
        when:
        def q = ChemTermsProcessor.parseParamString("logd=logD('7.4');atom_count=atomCount()");
        
        then:
        q.size() == 2
        q[0].propName == 'logd'
        q[0].chemTermsFunction == "logD('7.4')"
        q[1].propName == 'atom_count'
        q[1].chemTermsFunction == 'atomCount()'
        
    }
    
    def "query param with filter parsing"() {
        
        when:
        def q = ChemTermsProcessor.parseParamString("filter=atomCount()<6;filter=bondCount()<6;logp=logP()");
        
        
        then:
        q.size() == 3
        
        q[0].propName == 'filter'
        q[0].chemTermsFunction == "atomCount()<6"
        q[0].isFilter() == true
        
        q[1].propName == 'filter'
        q[1].chemTermsFunction == "bondCount()<6"
        q[1].isFilter() == true
        
        q[2].propName == 'logp'
        q[2].chemTermsFunction == 'logP()'
        q[2].isFilter() == false
        
    }
    
    

    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:chemTermsCalculator")
                .process(new ChemTermsProcessor())
                .to('mock:result')
            }
        }
    }
}
