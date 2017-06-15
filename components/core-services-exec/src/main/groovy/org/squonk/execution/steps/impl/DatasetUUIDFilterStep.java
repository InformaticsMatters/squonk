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

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.BasicObject;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 29/12/15.
 */
public class DatasetUUIDFilterStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(DatasetUUIDFilterStep.class.getName());

    public static final String OPTION_UUIDS = StepDefinitionConstants.DatasetUUIDFilter.OPTION_UUIDS;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_INPUT;
        Dataset<? extends BasicObject> input = fetchMappedInput("input", Dataset.class, varman, true);
        String uuidsOpt = getOption(OPTION_UUIDS, String.class);
        if (uuidsOpt == null) {
            throw new IllegalStateException("UUIDs not defined. Should be present as option named " + OPTION_UUIDS);
        }
        LOG.info("UUIDs: " + uuidsOpt);
        Set<UUID> uuids = parseUUIDs(uuidsOpt);

        statusMessage = "Filtering ...";
        Stream <? extends BasicObject>output = input.getStream().filter((bo) -> uuids.contains(bo.getUUID()));
        Dataset<? extends BasicObject> results = new Dataset(output, deriveOutputDatasetMetadata(input.getMetadata()));

        createMappedOutput("output", Dataset.class, results, varman);
        statusMessage = generateStatusMessage(input.getSize(), results.getSize(), -1);
        LOG.info("Results: " + results.getMetadata());;
    }

    protected Set<UUID> parseUUIDs(String s) {
        String[] parts = s.split("[,\\n\\s]+");
        Set<UUID> set = new HashSet<>();
        for (int i = 0; i < parts.length ; i++) {
            String uuid = parts[i].trim();
            set.add(UUID.fromString(uuid));
        }
        return set;
    }

    protected DatasetMetadata deriveOutputDatasetMetadata(DatasetMetadata input) {
        if (input == null) {
            return new DatasetMetadata(BasicObject.class);
        } else {
            return new DatasetMetadata(input.getType(), input.getValueClassMappings(), 0, input.getProperties());
        }
    }


}
