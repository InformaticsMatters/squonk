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

import chemaxon.struc.MolBond
import chemaxon.struc.Molecule
import org.squonk.types.MoleculeObject
import org.apache.camel.CamelContext
import org.apache.camel.builder.ThreadPoolProfileBuilder
import org.apache.camel.spi.ThreadPoolProfile
import org.squonk.camel.CamelCommonConstants
import org.squonk.camel.chemaxon.processor.ChemAxonMoleculeProcessor
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.squonk.chemaxon.molecule.ChemTermsEvaluator
import org.squonk.data.Molecules
import org.squonk.dataset.MoleculeObjectDataset

import spock.lang.IgnoreIf

/**
 * Created by timbo on 14/04/2014.
 */
@IgnoreIf({ System.getenv('CHEMAXON_LICENCE_ABSENT') != null })
class ChemaxonCalculatorsRoutesSpec extends CamelSpecificationBase {

    def mols = [
        new MoleculeObject('C'),
        new MoleculeObject('CC'),
        new MoleculeObject('CCC')
            ]

    def 'logp multiple MoleculeObject as stream'() {

        when:
        def results = template.requestBody(ChemaxonCalculatorsRouteBuilder.CHEMAXON_LOGP, new MoleculeObjectDataset(mols))

        then:
        results instanceof MoleculeObjectDataset
        results.stream.count() == 3
    }

    def 'logs multiple MoleculeObject as stream'() {

        when:
        def results = template.requestBody(ChemaxonCalculatorsRouteBuilder.CHEMAXON_LOGS, new MoleculeObjectDataset(mols))
        def list = results.items

        then:
        results instanceof MoleculeObjectDataset
        list.size() == 3
        list.each {
            assert it.values['AqSol_CXN_7.4'] != null
        }
    }

    def 'logp file as stream'() {

        when:
        def results = template.requestBody(ChemaxonCalculatorsRouteBuilder.CHEMAXON_LOGP, Molecules.nci100Dataset())
        println results

        then:
        results instanceof MoleculeObjectDataset
        results.stream.count() == 100
    }

    def 'cns mpo as stream'() {

        when:
        def results = template.requestBody(ChemaxonCalculatorsRouteBuilder.CHEMAXON_CNS_MPO, Molecules.nci100Dataset())
        def list = results.items

        then:
        results instanceof MoleculeObjectDataset

        list.size() == 100
        list.each {
            assert it.values['LogP_CXN'] != null
            assert it.values['LogD_CXN_7.4'] != null
        }
    }
    
    def 'filter as stream'() {
        
        when:
        def results = template.requestBody(ChemaxonCalculatorsRouteBuilder.CHEMAXON_DRUG_LIKE_FILTER,  new MoleculeObjectDataset(mols))
                
        then:
        results.stream.count() < 4
       
    }
    
    def 'filter dynamic'() {

        when:
        long t0 = System.currentTimeMillis()
        def results = template.requestBodyAndHeader(
            ChemaxonCalculatorsRouteBuilder.CHEMAXON_CHEMTERMS,  Molecules.nci100Dataset(),
            ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION, "filter=mass()<250"
        )
        long t1 = System.currentTimeMillis()
        long size = results.stream.count()
        long t2 = System.currentTimeMillis()
        //println "dynamic filter down to $size first in ${t1-t0}ms last in ${t2-t0}ms"
        then:
        size < 100
        size > 0

    }

    def 'filter as stream concurrent'() {
        println "filter as stream concurrent()"

        when:
        def results = []
        (1..10).each {
            // asyncRequestBody() returns a Future
            results << template.asyncRequestBody(ChemaxonCalculatorsRouteBuilder.CHEMAXON_DRUG_LIKE_FILTER,  Molecules.nci100Dataset())
        }

        then:
        results.size() == 10
        int s0 = results[0].get().getStream().count()
        (1..9).each {
            def result = results[it].get()
            long count = result.stream.count()
            //println "result $it count is $count"
            assert count == s0
        }
    }

    def 'multiple props'() {

        when:
        def results = template.requestBody(ChemaxonCalculatorsRouteBuilder.CHEMAXON_ATOM_BOND_COUNT, new MoleculeObjectDataset(mols))
        def result0 = results.items[0]

        then:
        result0.getValue(ChemTermsEvaluator.ATOM_COUNT) != null
        result0.getValue(ChemTermsEvaluator.BOND_COUNT) != null
    }

    def 'aromatize molecule'() {

        when:
        def results = template.requestBody(ChemaxonCalculatorsRouteBuilder.CHEMAXON_AROMATIZE, new MoleculeObjectDataset([new MoleculeObject('C1=CC=CC=C1')]))
        def result0 = results.items[0]

        then:
        result0.getRepresentation(Molecule.class.getName()).getBond(0).getType() == MolBond.AROMATIC
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
        context.addRoutes(new ChemaxonCalculatorsRouteBuilder())
    }
}
