/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

package org.squonk.chemaxon.services

import org.apache.camel.CamelContext
import org.apache.camel.builder.ThreadPoolProfileBuilder
import org.apache.camel.spi.ThreadPoolProfile
import org.squonk.camel.CamelCommonConstants
import org.squonk.camel.chemaxon.processor.screening.MoleculeScreenerProcessor
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.types.MoleculeObject

import spock.lang.IgnoreIf

/**
 * Created by timbo on 14/04/2014.
 */
@IgnoreIf({ System.getenv('CHEMAXON_LICENCE_ABSENT') != null })
class ChemaxonDescriptorsRoutesSpec extends CamelSpecificationBase {

    def mols = [
            new MoleculeObject('C'),
            new MoleculeObject('CC'),
            new MoleculeObject('CCC')
    ]

    def 'ecfp4 screening'() {

        def headers = [
                (MoleculeScreenerProcessor.HEADER_THRESHOLD): 0.5d,
                (MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE + '_source'): 'CC',
                (MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE + '_format'): 'smiles'
        ]

        when:
        def results = template.requestBodyAndHeaders(
                ChemaxonDescriptorsRouteBuilder.CHEMAXON_SCREENING_ECFP4,
                new MoleculeObjectDataset(mols),
                headers)

        then:
        results instanceof MoleculeObjectDataset
        results.stream.count() == 1
    }

    def 'pharmacophore screening'() {

        def headers = [
                (MoleculeScreenerProcessor.HEADER_THRESHOLD): 0.5d,
                (MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE + '_source'): 'CC',
                (MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE + '_format'): 'smiles'
        ]

        when:
        def results = template.requestBodyAndHeaders(
                ChemaxonDescriptorsRouteBuilder.CHEMAXON_SCREENING_PHARMACOPHORE,
                new MoleculeObjectDataset(mols),
                headers)

        then:
        results instanceof MoleculeObjectDataset
        results.stream.count() == 1
    }

    def 'spherex clustering'() {

        def headers = [
                (MoleculeScreenerProcessor.HEADER_THRESHOLD): 0.5d
        ]

        when:
        def results = template.requestBodyAndHeaders(
                ChemaxonDescriptorsRouteBuilder.CHEMAXON_CLUSTERING_SPHEREX_ECFP4,
                new MoleculeObjectDataset(mols),
                headers)

        then:
        results instanceof MoleculeObjectDataset
        results.stream.count() == 3
    }


    @Override
    CamelContext createCamelContext() {
        CamelContext camelContext = super.createCamelContext()
        ThreadPoolProfile profile = new ThreadPoolProfileBuilder(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME).poolSize(5).maxPoolSize(20).build();
        camelContext.getExecutorServiceManager().registerThreadPoolProfile(profile);
        return camelContext
    }

    @Override
    void addRoutes(CamelContext context) {
        context.addRoutes(new ChemaxonDescriptorsRouteBuilder())
    }
}
