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

package org.squonk.camel.chemaxon.processor.clustering

import com.chemaxon.descriptors.fingerprints.ecfp.*
import com.chemaxon.descriptors.metrics.BinaryMetrics
import org.apache.camel.CamelContext
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.squonk.types.MoleculeObject;
import org.apache.camel.builder.RouteBuilder
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.dataset.MoleculeObjectDataset

import spock.lang.IgnoreIf

/**
 * Created by timbo on 14/04/2014.
 */
@IgnoreIf({ System.getenv('CHEMAXON_LICENCE_ABSENT') != null })
class SphereExclusionClusteringProcessorSpec extends CamelSpecificationBase {

    
    def "cluster stream"() {
        given:
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
            
        when:
        template.sendBody('direct:simple', dataset)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0].getStream().collect()
        result.size() == 36
        int max = 0
        result.each {
            Integer cluster = it.getValue('cluster')
            assert cluster != null
            if (cluster > max) { max = cluster }
        }
        max > 0

    }
    
    def "renamed"() {
        given:
        List<MoleculeObject> mols = [
            new MoleculeObject("CC1=CC(=O)C=CC1=O"),
            new MoleculeObject("S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4"),
            new MoleculeObject("OC1=C(Cl)C=C(C=C1[N+]([O-])=O)[N+]([O-])=O"),
            new MoleculeObject("[O-][N+](=O)C1=CNC(=N)S1"),
            new MoleculeObject("NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O"),
            new MoleculeObject("OC(=O)C1=C(C=CC=C1)C2=C3C=CC(=O)C(=C3OC4=C2C=CC(=C4Br)O)Br"),
            new MoleculeObject("CN(C)C1=C(Cl)C(=O)C2=C(C=CC=C2)C1=O"),
            new MoleculeObject("CC1=C(C2=C(C=C1)C(=O)C3=CC=CC=C3C2=O)[N+]([O-])=O"),
            new MoleculeObject("CC(=NO)C(C)=NO"),
            new MoleculeObject("C1=CC=C(C=C1)P(C2=CC=CC=C2)C3=CC=CC=C3")
        ]

        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
            
        when:
        template.sendBody('direct:renamed', new MoleculeObjectDataset(mols))

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0].getStream().collect()
        result.size() == 10
        result.each {
            Integer cluster = it.getValue('abcd')
            assert cluster != null
        }
    }

    @Override
    void addRoutes(CamelContext context) {
              
        context.addRoutes(new RouteBuilder() {
            
            EcfpGenerator gen = new EcfpParameters().getDescriptorGenerator(); // default ECFP
            
            public void configure() {
                from("direct:simple")
                .process(new SphereExclusionClusteringProcessor(
                        gen, gen.getBinaryMetricsComparator(BinaryMetrics.BINARY_TANIMOTO)))
                .to('mock:result')
                
                from("direct:renamed")
                .process(new SphereExclusionClusteringProcessor(
                        gen, gen.getBinaryMetricsComparator(BinaryMetrics.BINARY_TANIMOTO))
                    .clusterPropertyName('abcd'))
                .to('mock:result')
            }
        })
    }
}
