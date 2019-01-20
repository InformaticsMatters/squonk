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

package org.squonk.camel.cdk.processor

import org.apache.camel.CamelContext
import org.squonk.cdk.molecule.MolecularDescriptors

import org.squonk.camel.testsupport.CamelSpecificationBase
import org.squonk.types.MoleculeObject
import org.squonk.dataset.MoleculeObjectDataset
import org.apache.camel.builder.RouteBuilder
import org.squonk.util.ExecutionStats
import org.squonk.util.StatsRecorder

import java.util.stream.Stream

/**
 *
 * @author timbo
 */
class CDKMolecularDescriptorProcessorSpec extends CamelSpecificationBase {
    
    def resultEndpoint
    
    void "simple calcs"() {
        
        setup:
        
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        List<MoleculeObject> mols = new ArrayList<>()
        mols << new MoleculeObject("CC")
        mols << new MoleculeObject("CCC")
        
        when:
        template.sendBody('direct:start', new MoleculeObjectDataset(mols))
        
        then:
        resultEndpoint.assertIsSatisfied()
        def mods = resultEndpoint.receivedExchanges.in.body[0]
        mods instanceof MoleculeObjectDataset
        def results = mods.items
        results.size() == 2
        results[0].getValue(MolecularDescriptors.XLOGP_XLOGP) != null
        results[0].getValue(MolecularDescriptors.WIENER_PATH) != null
        results[0].getValue(MolecularDescriptors.WIENER_POLARITY) != null
        results[0].getValue(MolecularDescriptors.ALOGP_ALOPG) != null
        results[0].getValue(MolecularDescriptors.ALOGP_AMR) != null
        results[0].getValue(MolecularDescriptors.HBOND_ACCEPTOR_COUNT) != null
        results[0].getValue(MolecularDescriptors.HBOND_DONOR_COUNT) != null
    }

    void "record stats"() {
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)

        List<MoleculeObject> mols = new ArrayList<>()
        mols << new MoleculeObject("CC")
        mols << new MoleculeObject("CCC")

        StatsRecorder recorder = new StatsRecorder("job1") {

            Map data

            @Override
            protected void sendStats(ExecutionStats executionStats) {
                this.data = executionStats.getData()
            }
        }

        when:
        Stream st = mols.stream()
        def results = template.requestBodyAndHeaders('direct:start', new MoleculeObjectDataset(st), [(StatsRecorder.HEADER_STATS_RECORDER):recorder])
        def items = results.dataset.items // need to get the items so that the stream is processed and closed

        then:
        resultEndpoint.assertIsSatisfied()
        recorder.data.size() == 5
        recorder.data*.value == [2, 2, 2, 2, 2]
    }


    @Override
    void addRoutes(CamelContext context) {
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start")
                .process(new CDKMolecularDescriptorProcessor()
                    .calculate(MolecularDescriptors.Descriptor.XLogP)
                    .calculate(MolecularDescriptors.Descriptor.ALogP)
                    .calculate(MolecularDescriptors.Descriptor.HBondDonorCount)
                    .calculate(MolecularDescriptors.Descriptor.HBondAcceptorCount)
                    .calculate(MolecularDescriptors.Descriptor.WienerNumbers))
                .to('mock:result')
            }
        })
    }
	
}

