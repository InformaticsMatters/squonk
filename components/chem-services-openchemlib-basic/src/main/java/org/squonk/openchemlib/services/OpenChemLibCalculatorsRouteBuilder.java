package org.squonk.openchemlib.services;

import org.apache.camel.builder.RouteBuilder;
import org.squonk.camel.openchemlib.processor.OCLVerifyStructureProcessor;
import org.squonk.camel.openchemlib.processor.PredictorProcessor;
import org.squonk.openchemlib.predict.OCLLogPPredictor;
import org.squonk.openchemlib.predict.OCLPSAPredictor;
import org.squonk.openchemlib.predict.OCLSolubilityPredictor;

/**
 * Created by timbo on 06/04/16.
 */
public class OpenChemLibCalculatorsRouteBuilder extends RouteBuilder {

    static final String OCL_STRUCTURE_VERIFY = "direct:structure_verify";
    static final String OCL_LOGP = "direct:logp";
    static final String OCL_LOGS = "direct:logs";
    static final String OCL_PSA = "direct:psa";

    @Override
    public void configure() throws Exception {

        from(OCL_STRUCTURE_VERIFY)
                .log("OCL_STRUCTURE_VERIFY starting")
                .threads().executorServiceRef(CustomCamelContext.THREAD_POOL_PROFILE)
                .process(new OCLVerifyStructureProcessor());

        from(OCL_LOGP )
                .log("OCL_LOGP starting")
                //.threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new PredictorProcessor().calculate(new OCLLogPPredictor()));

        from(OCL_LOGS )
                .log("OCL_LOGS starting")
                //.threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new PredictorProcessor().calculate(new OCLSolubilityPredictor()));

        from(OCL_PSA )
                .log("OCL_PSA starting")
                //.threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new PredictorProcessor().calculate(new OCLPSAPredictor()));

    }
}
