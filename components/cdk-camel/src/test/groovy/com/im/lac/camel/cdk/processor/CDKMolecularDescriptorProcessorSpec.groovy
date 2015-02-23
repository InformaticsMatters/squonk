package com.im.lac.camel.cdk.processor

import spock.lang.Specification
import com.im.lac.cdk.molecule.MolecularDescriptors

import com.im.lac.camel.testsupport.CamelSpecificationBase
import com.im.lac.types.MoleculeObject
import org.apache.camel.builder.RouteBuilder

/**
 *
 * @author timbo
 */
class CDKMolecularDescriptorProcessorSpec extends CamelSpecificationBase {
    
    def resultEndpoint
    
    void "simple xlogp"() {
        
        setup:
        
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        List<MoleculeObject> mols = new ArrayList<>()
        mols << new MoleculeObject("CC")
        mols << new MoleculeObject("CCC")
        
        when:
        template.sendBody('direct:start', mols)
        
        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0].collect()
        result.size() == 2
        result[0].getValue(MolecularDescriptors.XLOGP_XLOGP) != null
        result[0].getValue(MolecularDescriptors.WIENER_PATH) != null
        result[0].getValue(MolecularDescriptors.WIENER_POLARITY) != null
        result[0].getValue(MolecularDescriptors.ALOGP_ALOPG) != null
        result[0].getValue(MolecularDescriptors.ALOGP_ALOPG2) != null
        result[0].getValue(MolecularDescriptors.ALOGP_AMR) != null
        result[0].getValue(MolecularDescriptors.HBOND_ACCEPTOR_COUNT) != null
        result[0].getValue(MolecularDescriptors.HBOND_DONOR_COUNT) != null
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

