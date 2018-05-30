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

package org.squonk.chemaxon.services;

import chemaxon.license.LicenseException;
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpGenerator;
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpParameters;
import com.chemaxon.descriptors.fingerprints.pf2d.PfGenerator;
import com.chemaxon.descriptors.fingerprints.pf2d.PfParameters;
import com.chemaxon.descriptors.metrics.BinaryMetrics;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.chemaxon.processor.clustering.SphereExclusionClusteringProcessor;
import org.squonk.camel.chemaxon.processor.screening.MoleculeScreenerProcessor;
import org.squonk.chemaxon.screening.MoleculeScreener;
import org.apache.camel.builder.RouteBuilder;

import java.util.logging.Logger;

/**
 * Routes that perform screening and clustering using molecular descriptors
 *
 * @author timbo
 */
public class ChemaxonDescriptorsRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(ChemaxonDescriptorsRouteBuilder.class.getName());

    public static final String CHEMAXON_SCREENING_ECFP4 = "direct:screening/ecfp4";
    public static final String CHEMAXON_SCREENING_PHARMACOPHORE = "direct:screening/pharmacophore";
    public static final String CHEMAXON_CLUSTERING_SPHEREX_ECFP4 = "direct:clustering/spherex/ecfp4";

    @Override
    public void configure() throws Exception {

        // virtual screening using ECFP similarity
        try {
            EcfpParameters ecfpParams = EcfpParameters.createNewBuilder().build();
            EcfpGenerator ecfpGenerator = ecfpParams.getDescriptorGenerator();
            MoleculeScreener ecfpScreener = new MoleculeScreener(ecfpGenerator, ecfpGenerator.getDefaultComparator());

            from(CHEMAXON_SCREENING_ECFP4)
                    .log("CHEMAXON_SCREENING_ECFP starting")
                    .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                    .process(new MoleculeScreenerProcessor(ecfpScreener))
                    .log("CHEMAXON_SCREENING_ECFP finished");
        } catch (LicenseException le) {
            LOG.warning("Could not create route " + CHEMAXON_SCREENING_ECFP4 + " as licenses not present");
        }

        // virtual screening using pharmacophore similarity
        try {
            PfParameters pfParams = PfParameters.createNewBuilder().build();
            PfGenerator pfGenerator = pfParams.getDescriptorGenerator();
            MoleculeScreener pfScreener = new MoleculeScreener(pfGenerator, pfGenerator.getDefaultComparator());

            from(CHEMAXON_SCREENING_PHARMACOPHORE)
                    .log("CHEMAXON_SCREENING_PHARMACOPHORE starting")
                    .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                    .process(new MoleculeScreenerProcessor(pfScreener))
                    .log("CHEMAXON_SCREENING_PHARMACOPHORE finished");
        } catch (LicenseException le) {
            LOG.warning("Could not create route " + CHEMAXON_SCREENING_PHARMACOPHORE + " as licenses not present");
        }

        // clustering using sphere exclusion clustering and ECPF4 descriptors
        try {
            EcfpGenerator gen = new EcfpParameters().getDescriptorGenerator(); // default ECFP

            from(CHEMAXON_CLUSTERING_SPHEREX_ECFP4)
                    .log("CHEMAXON_CLUSTERING_SPHEREX_ECFP4 starting")
                    .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                    .process(new SphereExclusionClusteringProcessor(
                            gen, gen.getBinaryMetricsComparator(BinaryMetrics.BINARY_TANIMOTO)))
                    .log("CHEMAXON_CLUSTERING_SPHEREX_ECFP4 finished");
        } catch (LicenseException le) {
            LOG.warning("Could not create route " + CHEMAXON_CLUSTERING_SPHEREX_ECFP4 + " as licenses not present");
        }
    }
}
