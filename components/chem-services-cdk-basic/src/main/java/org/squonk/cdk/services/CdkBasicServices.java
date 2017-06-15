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

package org.squonk.cdk.services;

import org.squonk.dataset.ThinDescriptor;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.OptionDescriptor;

/**
 * Created by timbo on 14/11/16.
 */
public class CdkBasicServices {

    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_VERIFY = createServiceDescriptor(
            "cdk.calculators.verify",
            "Verify structure (CDK)",
            "Verify that the molecules are valid according to CDK",
            new String[]{"verify", "cdk"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Verify+structure+%28CDK%29",
            "icons/properties_add.png",
            "verify",
            new OptionDescriptor[]{OptionDescriptor.FILTER_MODE},
            ThinDescriptor.DEFAULT_FILTERING_THIN_DESCRIPTOR);

    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_LOGP = createServiceDescriptor(
            "cdk.logp", "LogP (CDK)", "LogP predictions for XLogP, ALogP and AMR using CDK",
            new String[]{"logp", "partitioning", "molecularproperties", "cdk"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/LogP+%28CDK%29",
            "icons/properties_add.png", "logp", null, null);

    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_HBA_HBD = createServiceDescriptor(
            "cdk.donors_acceptors", "HBA & HBD (CDK)", "H-bond donor and acceptor counts using CDK",
            new String[]{"hbd", "donors", "hba", "acceptors", "topology", "molecularproperties", "cdk"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/HBA+%26+HBD+%28CDK%29",
            "icons/properties_add.png", "donors_acceptors", null, null);

    static final HttpServiceDescriptor SERVICE_DESCRIPTOR_WIENER_NUMBERS = createServiceDescriptor(
            "cdk.wiener_numbers", "Wiener Numbers (CDK)", "Wiener path and polarity numbers using CDK",
            new String[]{"wiener", "topology", "molecularproperties", "cdk"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Wiener+Numbers+%28CDK%29",
            "icons/properties_add.png", "wiener_numbers", null, null);

    static final HttpServiceDescriptor[] ALL = new HttpServiceDescriptor[] {
            SERVICE_DESCRIPTOR_VERIFY,
            SERVICE_DESCRIPTOR_LOGP,
            SERVICE_DESCRIPTOR_HBA_HBD,
            SERVICE_DESCRIPTOR_WIENER_NUMBERS
    };


    private static HttpServiceDescriptor createServiceDescriptor(
            String id, String name, String description, String[] tags,
            String resourceUrl, String icon, String endpoint,
            OptionDescriptor[] options, ThinDescriptor thinDescriptor
            ) {

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
