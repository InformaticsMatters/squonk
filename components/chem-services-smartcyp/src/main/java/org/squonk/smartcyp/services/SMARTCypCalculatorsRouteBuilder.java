package org.squonk.smartcyp.services;

import org.apache.camel.builder.RouteBuilder;
import org.squonk.camel.processor.SMARTCypProcessor;


/**
 * Created by timbo on 06/04/16.
 */
public class SMARTCypCalculatorsRouteBuilder extends RouteBuilder {


    static final String SMARTCyp_predict = "direct:smartcyp";


    @Override
    public void configure() throws Exception {


        from(SMARTCyp_predict)
                .log("SMARTCyp_predict starting")
                //.threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new SMARTCypProcessor());

    }
}
