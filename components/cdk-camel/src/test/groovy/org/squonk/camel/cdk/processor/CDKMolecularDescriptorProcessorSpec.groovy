package org.squonk.camel.cdk.processor

import org.squonk.cdk.molecule.MolecularDescriptors

import org.squonk.camel.testsupport.CamelSpecificationBase
import com.im.lac.types.MoleculeObject
import org.squonk.dataset.MoleculeObjectDataset
import org.apache.camel.builder.RouteBuilder
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

            Map<String, Integer> executionStats

            @Override
            protected void sendStats(Map<String, Integer> executionStats) {
                super.sendStats(executionStats)
                this.executionStats = executionStats
            }
        }

        when:
        Stream st = mols.stream()
        def results = template.requestBodyAndHeaders('direct:start', new MoleculeObjectDataset(st), [STATS_RECORDER:recorder])
        def items = results.dataset.items // need to get the items so that the stream is processed and closed

        then:
        resultEndpoint.assertIsSatisfied()
        recorder.executionStats.size() == 5
        recorder.executionStats*.value == [2, 2, 2, 2, 2]
    }
    
    
    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
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
        }
    }
	
}

