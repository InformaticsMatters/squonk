package com.im.squonk.rdkit.services;

import com.im.lac.camel.CamelCommonConstants;
import com.im.lac.camel.rdkit.RDKitMoleculeProcessor;
import org.apache.camel.builder.RouteBuilder;
import static com.im.lac.rdkit.mol.EvaluatorDefintion.Function.*;

/**
 * Basic services based on CDK
 *
 * @author timbo
 */
public class RdkitCalculatorsRouteBuilder extends RouteBuilder {

    static final String RDKIT_LOGP = "direct:logp";

    @Override
    public void configure() throws Exception {

        from(RDKIT_LOGP)
                .log("RDKIT_LOGP starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new RDKitMoleculeProcessor().calculate("RDKit_LogP", LOGP))
                .log("RDKIT_LOGP finished");

    }
}
