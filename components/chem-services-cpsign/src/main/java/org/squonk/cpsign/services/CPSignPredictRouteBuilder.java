package org.squonk.cpsign.services;

import org.apache.camel.builder.RouteBuilder;
import org.squonk.camel.processor.CPSignTrainProcessor;


/**
 * Created by timbo on 06/04/16.
 */
public class CPSignPredictRouteBuilder extends RouteBuilder {


    static final String CPSign_train = "direct:cpsign_train";
    static final String CPSign_predict = "direct:cpsign_predict";


    @Override
    public void configure() throws Exception {

        from(CPSign_train)
                .log("CPSign_train starting")
                //.threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new CPSignTrainProcessor());

    }
}
