package org.squonk.cdk.services;

import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.cdk.processor.CDKMolecularDescriptorProcessor;
import org.squonk.cdk.molecule.MolecularDescriptors;
import org.apache.camel.builder.RouteBuilder;

/**
 * Basic services based on CDK
 *
 * @author timbo
 */
public class CdkCalculatorsRouteBuilder extends RouteBuilder {

    static final String CDK_LOGP = "direct:logp";
    static final String CDK_DONORS_ACCEPTORS = "direct:donors_acceptors";
    static final String CDK_WIENER_NUMBERS = "direct:wiener_numbers";

    @Override
    public void configure() throws Exception {

        from(CDK_LOGP )
                .log("CDK_LOGP starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new CDKMolecularDescriptorProcessor()
                        .calculate(MolecularDescriptors.Descriptor.XLogP)
                        .calculate(MolecularDescriptors.Descriptor.ALogP));

        from(CDK_DONORS_ACCEPTORS)
                .log("CDK_DONORS_ACCEPTORS starting")
                .process(new CDKMolecularDescriptorProcessor()
                        .calculate(MolecularDescriptors.Descriptor.HBondDonorCount)
                        .calculate(MolecularDescriptors.Descriptor.HBondAcceptorCount));

        from(CDK_WIENER_NUMBERS)
                .log("CDK_WIENER_NUMBERS starting")
                .process(new CDKMolecularDescriptorProcessor()
                        .calculate(MolecularDescriptors.Descriptor.WienerNumbers));

    }
}
