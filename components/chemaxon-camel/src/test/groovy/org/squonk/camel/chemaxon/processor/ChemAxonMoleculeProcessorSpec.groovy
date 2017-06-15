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

package org.squonk.camel.chemaxon.processor

import org.squonk.data.Molecules
import org.squonk.dataset.Dataset

import java.util.stream.*

import org.squonk.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class ChemAxonMoleculeProcessorSpec extends CamelSpecificationBase {
    
    //    String file = "../../data/testfiles/Building_blocks_GBP.sdf.gz"
    //    int count = 7003
    
    //    String file = "../../data/testfiles/nci100.smiles"
    //    int count = 100
    
//    String file = "../../data/testfiles/dhfr_standardized.sdf.gz"
//    int count = 756

     String file = "../../data/testfiles/Kinase_inhibs.sdf.gz"
    int count = 36

    
    void "propcalc sequential streaming"() {
        setup:
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        dataset.replaceStream(dataset.getStream().sequential())
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.requestBody('direct:streaming', dataset).getStream()
        List all = Collections.unmodifiableList(results.collect(Collectors.toList()))
        long t1 = System.currentTimeMillis()
        
        then:
        println "  number of mols: ${all.size()} generated in ${t1-t0}ms"
        all.size() == count
        
        
        cleanup:
        results.close()
        
    }
    
    void "propcalc parallel streaming"() {
        setup:
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        dataset.replaceStream(dataset.getStream().parallel())
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.requestBody('direct:streaming', dataset, Dataset.class).getStream()
        List all = Collections.unmodifiableList(results.collect(Collectors.toList()))
        long t1 = System.currentTimeMillis()
        
        then:
        println "  number of mols: ${all.size()} generated in ${t1-t0}ms"
        all.size() == count
        
        cleanup:
        results.close()
    }
    
    void "noop parallel streaming"() {
        setup:
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        dataset.replaceStream(dataset.getStream().parallel())
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.requestBody('direct:noop', dataset, Dataset.class).getStream()
        List all = Collections.unmodifiableList(results.collect(Collectors.toList()))
        long t1 = System.currentTimeMillis()
        
        then:
        println "  number of mols: ${all.size()} generated in ${t1-t0}ms"
        all.size() == count
        
        cleanup:
        results.close()
    }

    
    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {

                from("direct:streaming")
                .process(new ChemAxonMoleculeProcessor()
                    //.standardize("removefragment:method=keeplargest..aromatize..removeexplicith")
                    .molWeight()
                    .logP()
                    .donorCount()
                    .acceptorCount()
                    //.calculate("logd", "logD('7.4')")
                    .ringCount()
                    .rotatableBondCount()
                    //.transform("leconformer()")
                )       
                
                from("direct:noop")
                .process(new ChemAxonMoleculeProcessor()
                    .atomCount()
                )
            }
        }
    }
}
