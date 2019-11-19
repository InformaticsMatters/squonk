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

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.chembl.ChemblClient;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.MoleculeObject;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class ChemblActivitiesFetcherStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(ChemblActivitiesFetcherStep.class.getName());

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor(
            "core.chembl.activitiesfetcher.v1",
            "ChemblActivitiesFetcher",
            "Fetch activites and structures from ChEMBL using the ChEMBL REST API",
            new String[]{"dataset", "chembl", "assay", "rest", "activities"},
            null, "icons/import_external_service.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            null,
            IODescriptors.createMoleculeObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{

                    new OptionDescriptor<>(
                            String.class,
                            StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_ASSAY_ID,
                            "Assay ID",
                            "ChEMBL Asssay ID",
                            OptionDescriptor.Mode.User),
                    new OptionDescriptor<>(
                            String.class,
                            StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_PREFIX,
                            "Prefix", "Prefix for result fields", OptionDescriptor.Mode.User)
            },
            null, null, null,
            ChemblActivitiesFetcherStep.class.getName()
    );

    static final String OPTION_ASSAY_ID = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_ASSAY_ID;
    static final String OPTION_PREFIX = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_PREFIX;
    static final String OPTION_BATCH_SIZE = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_BATCH_SIZE;

    @Override
    public Map<String, Object> doExecute(Map<String, Object> inputs) throws Exception {
        dumpConfig(Level.INFO);

        int batchSize = getOption(OPTION_BATCH_SIZE, Integer.class, 500);
        String prefix = getOption(OPTION_PREFIX, String.class);
        String assayID = getOption(OPTION_ASSAY_ID, String.class);
        if (assayID == null) {
            throw new IllegalStateException("Assay ID to fetch not specified");
        }

        ChemblClient client = new ChemblClient();
        statusMessage = "Fetching data ...";
        Dataset<MoleculeObject> results = client.fetchActivitiesForAssay(assayID, batchSize, prefix);
        addResultsCounter(results);
        return Collections.singletonMap(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, results);
    }
}
