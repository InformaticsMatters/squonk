package org.squonk.openchemlib.services;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.openchemlib.processor.PredictorProcessor;
import org.squonk.openchemlib.predict.LogPPredictor;
import org.squonk.openchemlib.predict.PSAPredictor;
import org.squonk.openchemlib.predict.SolubilityPredictor;

/**
 * Created by timbo on 06/04/16.
 */
@ContextName("openchemlib")
public class OpenChemLibCalculatorsRouteBuilder extends RouteBuilder {

    static final String OCL_LOGP = "direct:logp";
    static final String OCL_LOGS = "direct:logs";
    static final String OCL_PSA = "direct:psa";

    @Override
    public void configure() throws Exception {

        from(OCL_LOGP )
                .log("OCL_LOGP starting")
                //.threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new PredictorProcessor().calculate(new LogPPredictor()));

        from(OCL_LOGS )
                .log("OCL_LOGS starting")
                //.threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new PredictorProcessor().calculate(new SolubilityPredictor()));

        from(OCL_PSA )
                .log("OCL_PSA starting")
                //.threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new PredictorProcessor().calculate(new PSAPredictor()));

    }
}
