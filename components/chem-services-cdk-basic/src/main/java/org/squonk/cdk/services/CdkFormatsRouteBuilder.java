package org.squonk.cdk.services;

import org.apache.camel.builder.RouteBuilder;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.cdk.processor.CDKDatasetConvertProcessor;

/**
 * Basic services based on CDK
 *
 * @author timbo
 */
public class CdkFormatsRouteBuilder extends RouteBuilder {


    public static final String CDK_DATASET_CONVERT = "direct:dataset_convert";

    @Override
    public void configure() throws Exception {

        from(CDK_DATASET_CONVERT)
                .log("CDK_DATASET_CONVERT starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new CDKDatasetConvertProcessor())
                .log("CDK_DATASET_CONVERT finished");


    }
}
