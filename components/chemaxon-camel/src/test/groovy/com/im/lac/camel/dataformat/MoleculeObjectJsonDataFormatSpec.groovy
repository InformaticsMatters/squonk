package com.im.lac.camel.dataformat

import com.im.lac.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.spi.DataFormat

import com.im.lac.types.MoleculeObjectIterable
import groovy.json.JsonLexer

/**
 *
 * @author timbo
 */
class MoleculeObjectJsonDataFormatSpec extends CamelSpecificationBase {
    
    def 'read molecules'() {

        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(100)
        File file = new File("../../data/testfiles/nci100.smiles")
        
        when:
        template.sendBody('direct:input', file)

        then:
        resultEndpoint.assertIsSatisfied()
    }
    
    def "lexer"() {
        
        setup:
        String input = '''\
{"format":"smiles","source":"CC1=CC(=O)C=CC1=O\n","values":{"field_0":"1"}},
{"format":"smiles","source":"S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4\n","values":{"field_0":"2"}},
{"format":"smiles","source":"OC1=C(Cl)C=C(C=C1[N+]([O-])=O)[N+]([O-])=O\n","values":{"field_0":"3"}},
{"format":"smiles","source":"[O-][N+](=O)C1=CNC(=N)S1\n","values":{"field_0":"4"}},
{"format":"smiles","source":"NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O\n","values":{"field_0":"5"}},
{"format":"smiles","source":"OC(=O)C1=C(C=CC=C1)C2=C3C=CC(=O)C(=C3OC4=C2C=CC(=C4Br)O)Br\n","values":{"field_0":"6"}}\n\
]'''
        def reader = new StringReader(input)
        JsonLexer lexer = new JsonLexer(reader)
        
        when:
        lexer.each {
            println "Type: ${it.type} Value: ${it.text}"
        }
        
        then:
        1 == 1
        
        cleanup:
        reader.close()
        
    }

    @Override
    RouteBuilder createRouteBuilder() {
        
        DataFormat mojson = new MoleculeObjectJsonDataFormat(); 
        
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                
                from("direct:input")
                .convertBodyTo(MoleculeObjectIterable.class)
                .split(body())
                .marshal(mojson)
                .log('JSON: ${body}')
                //.unmarshal().json(JsonLibrary.Jackson)
                //.log('MOL: ${body.sourceAsString}')
                .to("mock:result")
            }
        }
    }
	
}

