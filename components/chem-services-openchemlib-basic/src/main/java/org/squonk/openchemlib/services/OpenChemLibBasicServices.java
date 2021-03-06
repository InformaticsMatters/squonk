/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

package org.squonk.openchemlib.services;

import org.squonk.core.HttpServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;

import java.util.Arrays;

/**
 * Created by timbo on 14/11/16.
 */
public class OpenChemLibBasicServices {


    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_VERIFY = createServiceDescriptor(
            "ocl.calculators.verify",
            "Verify structure (OCL)",
            "Verify that the molecules are valid according to OpenChemLib",
            new String[]{"verify", "openchemlib"},
            "/docs/cells/Verify%20structure%20(OCL)/",
            "icons/properties_add.png",
            "verify",
            new OptionDescriptor[]{OptionDescriptor.FILTER_MODE_PASS},
            ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR);

    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_LOGP = createServiceDescriptor(
            "ocl.logp", "LogP (OpenChemLib)", "OpenChemLib LogP prediction",
            new String[]{"logp", "partitioning", "molecularproperties", "openchemlib"},
            "/docs/cells/LogP%20(OpenChemLib)/",
            "icons/properties_add.png", "logp", null, null);

    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_LOGS = createServiceDescriptor(
            "ocl.logs", "LogS (OpenChemLib)", "OpenChemLib Aqueous Solubility prediction",
            new String[]{"logs", "solubility", "molecularproperties", "openchemlib"},
            "/docs/cells/LogS%20(OpenChemLib)/",
            "icons/properties_add.png", "logs", null, null);

    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_PSA = createServiceDescriptor(
            "ocl.psa", "PSA (OpenChemLib)", "OpenChemLib Polar Surface Area prediction",
            new String[]{"psa", "tpsa", "molecularproperties", "openchemlib"},
            "/docs/cells/PSA%20(OpenChemLib)/",
            "icons/properties_add.png", "psa", null, null);


    static final HttpServiceDescriptor[] ALL = new HttpServiceDescriptor[]{
            SERVICE_DESCRIPTOR_VERIFY,
            SERVICE_DESCRIPTOR_LOGP,
            SERVICE_DESCRIPTOR_LOGS,
            SERVICE_DESCRIPTOR_PSA
    };

    static final ServiceDescriptorSet SD_SET = new ServiceDescriptorSet(
            "http://chemservices:8080/chem-services-openchemlib-basic/rest/v1/calculators",
            "http://chemservices:8080/chem-services-openchemlib-basic/rest/ping",
            Arrays.asList(ALL));

    private static HttpServiceDescriptor createServiceDescriptor(
            String id, String name, String description, String[] tags,
            String resourceUrl, String icon, String endpoint,
            OptionDescriptor[] options, ThinDescriptor thinDescriptor) {

        return new HttpServiceDescriptor(
                id,
                name,
                description,
                tags,
                resourceUrl,
                icon,
                IODescriptors.createMoleculeObjectDataset("input"),
                IODescriptors.createMoleculeObjectDataset("output"),
                options,
                thinDescriptor,
                StepDefinitionConstants.DatasetHttpExecutor.CLASSNAME,
                endpoint
        );
    }

}
