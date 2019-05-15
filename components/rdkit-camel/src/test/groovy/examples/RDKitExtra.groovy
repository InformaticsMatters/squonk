/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

import org.apache.camel.CamelContext
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

class RDKitExtraTest extends CamelSpecificationBase {
    
    
//    def 'sdf stream to mcs'() {
//        
//        setup:
//        def resultEndpoint = camelContext.getEndpoint('mock:result')
//        resultEndpoint.expectedMessageCount(1)
//        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))
//        
//        when:
//        template.sendBody('direct:findMCS', gzip)
//
//        then:
//        resultEndpoint.assertIsSatisfied()
//        def result = resultEndpoint.receivedExchanges.in.body[0]
//        result == "[#7](:,-[#6]-,:[#7]):,-[#6](:,-[#6])-,:[#7]"
//    }
//    def 'sdf stream to clusters'() {
//
//        setup:
//        def resultEndpoint = camelContext.getEndpoint('mock:result')
//        resultEndpoint.expectedMessageCount(1)
//        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))
//
//        when:
//        template.sendBody('direct:findclusts', gzip)
//
//        then:
//        resultEndpoint.assertIsSatisfied()
//        def result = resultEndpoint.receivedExchanges.in.body[0]
//        // For now we just find the size of the dist matrix because clustering doesn't work
//        result == 286146
//    }
//
//    def 'sdf stream to mmps'() {
//
//        setup:
//        def resultEndpoint = camelContext.getEndpoint('mock:result')
//        resultEndpoint.expectedMessageCount(1)
//        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))
//
//        when:
//        template.sendBody('direct:findmmps', gzip)
//
//        then:
//        resultEndpoint.assertIsSatisfied()
//        def result = resultEndpoint.receivedExchanges.in.body[0]
//        // For now we just find the size of the dist matrix because clustering doesn't work
//        result == 130
//    }

    @Override
    void addRoutes(CamelContext context) {
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:findMCS")
                .to("language:python:classpath:molecule_objects.py?transform=false")
                .to("language:python:classpath:find_mcs.py?transform=false")
                .to('mock:result')

                 from("direct:findclusts")
                .to("language:python:classpath:molecule_objects.py?transform=false")
                .setHeader('FINGERPRINT', constant("morgan"))
                .setHeader('CLUSTERING', constant("butina"))
                .setHeader('SIMILARITY', constant("dice"))
                .to("language:python:classpath:cluster_mols.py?transform=false")
                .to('mock:result')

                 from("direct:findmmps")
                .to("language:python:classpath:molecule_objects.py?transform=false")
                .to("language:python:classpath:find_mmps.py?transform=false")
                .to('mock:result')               
               
            }
        })
    }
}

