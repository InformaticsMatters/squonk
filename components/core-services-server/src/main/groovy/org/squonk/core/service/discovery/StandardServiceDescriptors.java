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

package org.squonk.core.service.discovery;

import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.impl.*;

import java.util.Arrays;

/**
 * Built in service descriptors. These typically run internally and don't need external services.
 *
 * Created by timbo on 13/03/17.
 */
public class StandardServiceDescriptors {


    public static final String URL = "squonk://standard";
    private final ServiceDescriptorSet services;


    StandardServiceDescriptors() {

        services = new ServiceDescriptorSet(URL, null, Arrays.asList(new ServiceDescriptor[]{

                DatasetSelectSliceStep.SERVICE_DESCRIPTOR,
                DatasetSelectRandomStep.SERVICE_DESCRIPTOR,
                DatasetMergerStep.SERVICE_DESCRIPTOR,
                DatasetEnricherStep.SERVICE_DESCRIPTOR,
                DatasetUUIDFilterStep.SERVICE_DESCRIPTOR,
                FragnetExpansionStep.SERVICE_DESCRIPTOR,
                DatasetSplitStep.SERVICE_DESCRIPTOR,
                DatasetSplitOnNullStep.SERVICE_DESCRIPTOR,
                SimpleSorterStep.SERVICE_DESCRIPTOR,
                DatasetSorterStep.SERVICE_DESCRIPTOR,
                DatasetSplitUsingExpressionStep.SERVICE_DESCRIPTOR,
                DatasetFieldFilterStep.SERVICE_DESCRIPTOR
                // add these once the portal has been refactored to used these as services
                //BasicObjectToMoleculeObjectStep.SERVICE_DESCRIPTOR,
                //ChemblActivitiesFetcherStep.SERVICE_DESCRIPTOR,
                //DatasetFilterGroovyStep.SERVICE_DESCRIPTOR,
                //DatasetMoleculesFromFieldStep.SERVICE_DESCRIPTOR,
                //SmilesStructuresStep.SERVICE_DESCRIPTOR,
                //MolfileReaderStep.SERVICE_DESCRIPTOR,
                //ValueTransformerStep.SERVICE_DESCRIPTOR,
        }));

    }

    protected ServiceDescriptorSet get() {
        return services;
    }


}
