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

package org.squonk.cpsign.services

import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.camel.processor.CPSignTrainProcessor
import org.squonk.types.CPSignTrainResult
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.types.MoleculeObject
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by timbo on 22/10/2016.
 */
class CPSignPredictRouteBuilderSpec extends Specification {

    @Shared
    CamelContext context = new DefaultCamelContext()

    void setupSpec() {
        context.addRoutes(new CPSignPredictRouteBuilder())
        context.start()
    }

    void cleanupSpec() {
        context?.stop()
    }

    void "train ccp classification"() {

        List items = Molecules.nci100Molecules()
        Random r = new Random()
        items.each {
            it.values['prop'] = r.nextBoolean() ? 'T' : 'F'
        }
        Dataset data = new Dataset(MoleculeObject.class, items)

        ProducerTemplate pt = context.createProducerTemplate()
        def headers = [
                (CPSignTrainProcessor.HEADER_FIELD_NAME): 'prop',
                (CPSignTrainProcessor.HEADER_PREDICT_METHOD): CPSignTrainResult.Method.CCP,
                (CPSignTrainProcessor.HEADER_PREDICT_TYPE): CPSignTrainResult.Type.Classification,
                (CPSignTrainProcessor.HEADER_VALUE_1): 'T',
                (CPSignTrainProcessor.HEADER_VALUE_2): 'F'
        ]

        when:
        CPSignTrainResult results = pt.requestBodyAndHeaders(CPSignPredictRouteBuilder.CPSign_train, data, headers)

        then:
        results != null
        results.path != null
    }

    void "train ccp regression"() {

        List items = Molecules.nci100Molecules()
        Random r = new Random()
        items.each {
            it.values['prop'] = (float)r.nextInt(10)
        }
        Dataset data = new Dataset(MoleculeObject.class, items)

        ProducerTemplate pt = context.createProducerTemplate()
        def headers = [
                (CPSignTrainProcessor.HEADER_FIELD_NAME): 'prop',
                (CPSignTrainProcessor.HEADER_PREDICT_METHOD): CPSignTrainResult.Method.CCP,
                (CPSignTrainProcessor.HEADER_PREDICT_TYPE): CPSignTrainResult.Type.Regression
        ]

        when:
        CPSignTrainResult results = pt.requestBodyAndHeaders(CPSignPredictRouteBuilder.CPSign_train, data, headers)

        then:
        results != null
        results.path != null
    }

}
