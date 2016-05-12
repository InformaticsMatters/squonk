package org.squonk.camel.cdk.processor

import org.squonk.cdk.molecule.MolecularDescriptors

import org.squonk.camel.testsupport.CamelSpecificationBase
import com.im.lac.types.MoleculeObject
import org.squonk.dataset.MoleculeObjectDataset
import org.apache.camel.builder.RouteBuilder

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

