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

package org.squonk.chemaxon.clustering

import org.squonk.chemaxon.molecule.MoleculeIterable
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpComparator
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpGenerator
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpParameters
import com.chemaxon.descriptors.metrics.BinaryMetrics
import org.squonk.chemaxon.molecule.MoleculeUtils
import org.squonk.types.MoleculeObjectIterable
import org.squonk.chemaxon.molecule.MoleculeObjectUtils
import org.squonk.util.IOUtils

import spock.lang.Specification

import spock.lang.IgnoreIf

/**
 *
 * @author timbo
 */
class SphereExclusionClustererSpec extends Specification {

    @IgnoreIf({ System.getenv('CHEMAXON_LICENCE_ABSENT') != null })
    void "test molecules"() {
        setup:
        InputStream is = new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz")
        MoleculeIterable iterable = MoleculeUtils.createIterable(is)
        EcfpGenerator gen = (new EcfpParameters()).getDescriptorGenerator()
        EcfpComparator comp = gen.getBinaryMetricsComparator(BinaryMetrics.BINARY_TANIMOTO)
        SphereExclusionClusterer clusterer = new SphereExclusionClusterer(gen, comp, 5, 10)

        
        when:
        def results = clusterer.clusterMolecules(iterable)
        def mols = results.collect()
        
        then:
        
        mols.size() == 756
        int max = 0
        mols.each {
           Integer cluster = it.getPropertyObject('cluster')
           assert cluster != null
           if (cluster > max) { max = cluster }
        }
        max > 0
        
        cleanup:
        IOUtils.closeIfCloseable(iterable)
    }

    @IgnoreIf({ System.getenv('CHEMAXON_LICENCE_ABSENT') != null })
    void "test molecule objects"() {
        setup:
        InputStream is = new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz")
        MoleculeObjectIterable iterable = MoleculeObjectUtils.createIterable(is)
        EcfpGenerator gen = (new EcfpParameters()).getDescriptorGenerator()
        EcfpComparator comp = gen.getBinaryMetricsComparator(BinaryMetrics.BINARY_TANIMOTO)
        SphereExclusionClusterer clusterer = new SphereExclusionClusterer(gen, comp, 5, 10)

        
        when:
        def results = clusterer.clusterMoleculeObjects(iterable)
        def mols = results.collect()
        
        then:
        
        mols.size() == 756
        int max = 0
        mols.each {
           Integer cluster = it.getValue('cluster')
           assert cluster != null
           if (cluster > max) { max = cluster }
        }
        max > 0
        //println "max $max"
        
        cleanup:
        IOUtils.closeIfCloseable(iterable)
    }
}

