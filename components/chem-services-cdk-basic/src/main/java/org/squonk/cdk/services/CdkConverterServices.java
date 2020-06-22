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

package org.squonk.cdk.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.camel.cdk.processor.CDKDatasetConvertProcessor;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;

import java.util.Arrays;

/**
 * Created by timbo on 14/11/16.
 */
public class CdkConverterServices {


    public static final HttpServiceDescriptor SERVICE_DESCRIPTOR_CONVERT_TO_SDF = new HttpServiceDescriptor(
            "cdk.export.sdf",
            "SDF Export (CDK)",
            "Convert to SD file format using CDK",
            new String[]{"export", "dataset", "sdf", "sdfile", "cdk"},
            null,
            "default_icon.png",
            new IODescriptor[]{IODescriptors.createMoleculeObjectDataset("input")},
            new IODescriptor[]{IODescriptors.createSDF("output")},
            null,
            StepDefinitionConstants.DatasetHttpExecutor.CLASSNAME,
            "dataset_to_sdf"
    );

    public static final HttpServiceDescriptor SERVICE_DESCRIPTOR_CONVERT_DATASET = new HttpServiceDescriptor(
            "cdk.dataset.convert.molecule.format",
            "Convert molecule format",
            "Convert molecule format for a Dataset using CDK",
            new String[]{"convert", "dataset", "format", "cdk"},
            null,
            "transform_molecule.png",
            new IODescriptor[]{IODescriptors.createMoleculeObjectDataset("input")},
            new IODescriptor[]{IODescriptors.createMoleculeObjectDataset("output")},
            new OptionDescriptor[]{
                    new OptionDescriptor<>(String.class, "query." + CDKDatasetConvertProcessor.HEADER_MOLECULE_FORMAT, "Molecule format", "Format to convert molecules to",
                            OptionDescriptor.Mode.User)
                            .withDefaultValue("mol")
                            .withValues(new String[] {"mol", "mol:v2", "mol:v3", "smiles", "smiles-kekule"})
                            .withMinMaxValues(1, 1)
            },
            new ThinDescriptor[]{new ThinDescriptor("input", "output", false, false, null)},
            StepDefinitionConstants.DatasetHttpExecutor.CLASSNAME,
            "dataset_convert_format"
    );

    static final HttpServiceDescriptor[] ALL = new HttpServiceDescriptor[]{
            SERVICE_DESCRIPTOR_CONVERT_TO_SDF, SERVICE_DESCRIPTOR_CONVERT_DATASET
    };

    public static ServiceDescriptorSet SD_SET = new ServiceDescriptorSet(
            "http://chemservices:8080/chem-services-cdk-basic/rest/v1/converters",
            "http://chemservices:8080/chem-services-cdk-basic/rest/ping",
            Arrays.asList(ALL));
}
