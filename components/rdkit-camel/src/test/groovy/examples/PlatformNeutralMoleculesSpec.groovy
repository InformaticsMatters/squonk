/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example

import org.squonk.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

class PlatformNeutralMoleculesSpec extends CamelSpecificationBase {
    
    
//    def 'smiles to molecules'() {
//        
//        setup:
//        def resultEndpoint = camelContext.getEndpoint('mock:result')
//        resultEndpoint.expectedMessageCount(1)
//        File file = new File("../../data/testfiles/nci1000.smiles")
//        
//        when:
//        template.sendBody('direct:handleMoleculeObjects', file)
//
//        then:
//        resultEndpoint.assertIsSatisfied()
//        def result = resultEndpoint.receivedExchanges.in.body[0]
//        result == 1000
//    }
//
//    def 'smiles to molecules lipinski'() {
//
//        setup:
//        def resultEndpoint = camelContext.getEndpoint('mock:result')
//        resultEndpoint.expectedMessageCount(1)
//        File file = new File("../../data/testfiles/nci1000.smiles")
//
//        when:
//        template.sendBody('direct:convertToMolsFilter', file)
//
//        then:
//        resultEndpoint.assertIsSatisfied()
//        def result = resultEndpoint.receivedExchanges.in.body[0]
//        result == 93
//    }
//
//
//    def 'InputStream to molecules'() {
//        setup:
//        def resultEndpoint = camelContext.getEndpoint('mock:result')
//        resultEndpoint.expectedMessageCount(1)
//        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))
//
//       when:
//        template.sendBody('direct:handleMoleculeObjects', gzip)
//
//        then:
//        resultEndpoint.assertIsSatisfied()
//        def result = resultEndpoint.receivedExchanges.in.body[0]
//        result == 756 // should be 756
//
//    }
//
//    def 'InputStream to threaded molecule filter'() {
//        setup:
//        def resultEndpoint = camelContext.getEndpoint('mock:result')
//        resultEndpoint.expectedMessageCount(1)
//        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz"))
//
//        when:
//        template.sendBody('direct:handleMoleculeObjects', gzip)
//
//        then:
//        resultEndpoint.assertIsSatisfied()
//        def result = resultEndpoint.receivedExchanges.in.body[0]
//        result == 36
//
//        cleanup:
//        gzip.close()
//    }

    
   @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:handleMoleculeObjects")
                .to("language:python:classpath:molecule_objects.py?transform=false")
                .setHeader('FUNCTION', constant("num_hba"))
                .to("language:python:classpath:calc_props_thread.py?transform=false")
                .to("language:python:classpath:molecule_counter.py?transform=false")
                .to('mock:result')


                from("direct:convertToMolsFilter")
                .to("language:python:classpath:molecule_objects.py?transform=false")
                .setHeader('FUNCTION', constant("-1<num_hbd<6"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")
                .setHeader('FUNCTION', constant("-1<num_hba<11"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")
                .setHeader('FUNCTION', constant("5<mol_logp<100"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")                
                .setHeader('FUNCTION', constant("0<mol_mr<500"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")
                .to("language:python:classpath:molecule_counter.py?transform=false")
                .to('mock:result')
                
            }
        }
    }
}

