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

package org.squonk.core.service.discovery;

import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;
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

                // this one allows to execute arbitary script in the specified docker container
                GenericDatasetDockerExecutorStep.SERVICE_DESCRIPTOR,
                DatasetSelectSliceStep.SERVICE_DESCRIPTOR,
                DatasetSelectRandomStep.SERVICE_DESCRIPTOR,
                DatasetMergerStep.SERVICE_DESCRIPTOR,
                DatasetEnricherStep.SERVICE_DESCRIPTOR,
                DatasetUUIDFilterStep.SERVICE_DESCRIPTOR
        }));

    }

    protected ServiceDescriptorSet get() {
        return services;
    }


}
