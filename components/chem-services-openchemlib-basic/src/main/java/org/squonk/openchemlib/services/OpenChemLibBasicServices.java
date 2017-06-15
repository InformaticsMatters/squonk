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

package org.squonk.openchemlib.services;

import org.squonk.core.HttpServiceDescriptor;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;

/**
 * Created by timbo on 14/11/16.
 */
public class OpenChemLibBasicServices {


    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_VERIFY = createServiceDescriptor(
            "ocl.calculators.verify",
            "Verify structure (OCL)",
            "Verify that the molecules are valid according to OpenChemLib",
            new String[]{"verify", "openchemlib"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Verify+structure+%28OCL%29",
            "icons/properties_add.png",
            "verify",
            new OptionDescriptor[]{OptionDescriptor.FILTER_MODE},
            ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR);

    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_LOGP = createServiceDescriptor(
            "ocl.logp", "LogP (OpenChemLib)", "OpenChemLib LogP prediction",
            new String[]{"logp", "partitioning", "molecularproperties", "openchemlib"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/LogP+%28OpenChemLib%29",
            "icons/properties_add.png", "logp", null, null);

    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_LOGS = createServiceDescriptor(
            "ocl.logs", "LogS (OpenChemLib)", "OpenChemLib Aqueous Solubility prediction",
            new String[]{"logs", "solubility", "molecularproperties", "openchemlib"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/LogS+%28OpenChemLib%29",
            "icons/properties_add.png", "logs", null, null);

    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_PSA = createServiceDescriptor(
            "ocl.psa", "PSA (OpenChemLib)", "OpenChemLib Polar Surface Area prediction",
            new String[]{"psa", "tpsa", "molecularproperties", "openchemlib"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/PSA+%28OpenChemLib%29",
            "icons/properties_add.png", "psa", null, null);


    static final HttpServiceDescriptor[] ALL = new HttpServiceDescriptor[]{
            SERVICE_DESCRIPTOR_VERIFY,
            SERVICE_DESCRIPTOR_LOGP,
            SERVICE_DESCRIPTOR_LOGS,
            SERVICE_DESCRIPTOR_PSA
    };

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
                StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME,
                endpoint
        );
    }

}
