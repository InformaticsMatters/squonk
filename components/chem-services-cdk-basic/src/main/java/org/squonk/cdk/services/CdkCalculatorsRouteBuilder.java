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
public class CDKCalculatorsRouteBuilder extends RouteBuilder {

    static final String CDK_LOGP = "direct:logp";

    @Override
    public void configure() throws Exception {

        from(CDK_LOGP)
                .log("CDK_LOGP starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new CDKMolecularDescriptorProcessor()
                        .calculate(MolecularDescriptors.Descriptor.XLogP)
                        .calculate(MolecularDescriptors.Descriptor.ALogP))
                .log("CDK_LOGP finished");

    }
}
