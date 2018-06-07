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

import org.squonk.camel.cdk.processor.CDKDatasetConvertProcessor;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;

import java.util.Arrays;

/**
 * Created by timbo on 14/11/16.
 */
public class CdkConverterServices {


    public static final HttpServiceDescriptor SERVICE_DESCRIPTOR_CONVERT_TO_SDF = createServiceDescriptor(
            "cdk.export.sdf", "SDF Export (CDK)", "Convert to SD file format using CDK",
            new String[]{"export", "dataset", "sdf", "sdfile", "cdk"},
            null,
            "default_icon.png", "dataset_to_sdf", null);

    public static final HttpServiceDescriptor SERVICE_DESCRIPTOR_CONVERT_DATASET = createServiceDescriptor(
            "cdk.dataset.convert.molecule.format", "Convert molecule format", "Convert molecule format for a Dataset using CDK",
            new String[]{"convert", "dataset", "format", "cdk"},
            null,
            "default_icon.png", "dataset_convert_format",
            new OptionDescriptor[]{
                    new OptionDescriptor<>(String.class, "query." + CDKDatasetConvertProcessor.HEADER_MOLECULE_FORMAT, "Molecule format", "Format to convert molecules to",
                            OptionDescriptor.Mode.User)
                            .withDefaultValue("mol")
                            .withValues(new String[] {"mol", "mol:v2", "mol:v3", "smiles"})
                            .withMinMaxValues(1, 1)
            }
    );

    static final HttpServiceDescriptor[] ALL = new HttpServiceDescriptor[]{
            SERVICE_DESCRIPTOR_CONVERT_TO_SDF, SERVICE_DESCRIPTOR_CONVERT_DATASET
    };

    public static ServiceDescriptorSet SD_SET = new ServiceDescriptorSet(
            "http://chemservices:8080/chem-services-cdk-basic/rest/v1/converters",
            "http://chemservices:8080/chem-services-cdk-basic/rest/ping",
            Arrays.asList(ALL));


    private static HttpServiceDescriptor createServiceDescriptor(String id, String name, String description, String[] tags, String resourceUrl, String icon, String endpoint, OptionDescriptor[] options) {

        return new HttpServiceDescriptor(
                id,
                name,
                description,
                tags,
                resourceUrl,
                icon,
                new IODescriptor[]{IODescriptors.createMoleculeObjectDataset("input")},
                new IODescriptor[]{IODescriptors.createSDF("output")},
                options,
                StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME,
                endpoint
        );
    }

}
