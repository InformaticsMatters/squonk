/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
