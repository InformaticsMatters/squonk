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

package org.squonk.rdkit.services

import org.squonk.camel.testsupport.CamelSpecificationBase
import org.squonk.types.MoleculeObject
import org.squonk.camel.CamelCommonConstants
import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.rdkit.mol.EvaluatorDefinition
import spock.lang.IgnoreIf

import static RdkitCalculatorsRouteBuilder.*

import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.builder.ThreadPoolProfileBuilder
import org.apache.camel.spi.ThreadPoolProfile

/**
 * Created by timbo on 14/04/2014.
 */
@IgnoreIf({ System.getenv("RDBASE") == null })
class RdkitCalculatorsRouteBuilderSpec extends CamelSpecificationBase {

    static MoleculeObjectDataset dataset = new MoleculeObjectDataset([
            new MoleculeObject('C'),
            new MoleculeObject('CC'),
            new MoleculeObject('CCC')])

    def 'logp'() {

        when:
        def results = template.requestBody(RdkitCalculatorsRouteBuilder.RDKIT_LOGP, dataset)

        then:
        results instanceof MoleculeObjectDataset
        def list = results.dataset.items
        list.size == 3
        list[0].getValue(EvaluatorDefinition.Function.LOGP.name) instanceof Float
    }
    
    def 'frac c sp3'() {
        
        when:
        def results = template.requestBody(RdkitCalculatorsRouteBuilder.RDKIT_FRACTION_C_SP3, dataset)

        then:
        results instanceof MoleculeObjectDataset
        def list = results.dataset.items
        list.size == 3
        list[0].getValue(EvaluatorDefinition.Function.FRACTION_C_SP3.name) instanceof Float
    }

    def 'lipinski'() {
        
        when:
        def results = template.requestBody(RDKIT_LIPINSKI, dataset)

        then:
        results instanceof MoleculeObjectDataset
        def list = results.dataset.items
        list.size == 3
        list[0].getValue(EvaluatorDefinition.Function.LIPINSKI_HBA.name) instanceof Integer
        list[0].getValue(EvaluatorDefinition.Function.LIPINSKI_HBD.name) instanceof Integer
        list[0].getValue(EvaluatorDefinition.Function.LOGP.name) instanceof Float
        list[0].getValue(EvaluatorDefinition.Function.EXACT_MW.name) instanceof Float
    }

    
    @Override
    CamelContext createCamelContext() {
        CamelContext camelContext = super.createCamelContext()
        ThreadPoolProfile profile = new ThreadPoolProfileBuilder(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME).poolSize(5).maxPoolSize(20).build();
        camelContext.getExecutorServiceManager().registerThreadPoolProfile(profile);
        return camelContext
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RdkitCalculatorsRouteBuilder()
    }
}
