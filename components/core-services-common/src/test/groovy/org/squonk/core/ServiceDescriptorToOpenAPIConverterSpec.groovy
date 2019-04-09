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

package org.squonk.core

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.OpenAPI
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.io.IODescriptors
import org.squonk.jobdef.JobStatus
import org.squonk.options.OptionDescriptor
import org.squonk.types.MoleculeObject
import org.squonk.types.NumberRange
import spock.lang.Specification

class ServiceDescriptorToOpenAPIConverterSpec extends Specification {

    void "create json schema object"() {

        when:
        Map schemas = ServiceDescriptorToOpenAPIConverter.createJsonSchema(MoleculeObject.class)

        then:
        schemas.size() == 1
    }

    void "to openapi"() {

        def converter = new ServiceDescriptorToOpenAPIConverter("http://squonk.it")
        def sd = new DefaultServiceDescriptor(
                "core.dataset.filter.slice.v1",
                "Dataset slice selector",
                "Generate a defined slice of the dataset",
                ["filter", "slice", "dataset"] as String[],
                null, "icons/filter.png",
                ServiceConfig.Status.ACTIVE,
                new Date(),
                IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
                IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
                [
                    new OptionDescriptor<>(Integer.class, StepDefinitionConstants.DatasetSelectSlice.OPTION_SKIP, "Number to skip", "The number of records to skip", OptionDescriptor.Mode.User),
                    new OptionDescriptor<>(Integer.class, StepDefinitionConstants.DatasetSelectSlice.OPTION_COUNT, "Number to include", "The number of records to include after skipping", OptionDescriptor.Mode.User)
                ] as OptionDescriptor[],
                null, null, null,
                "org.squonk.execution.steps.impl.DatasetSelectSliceStep"
        )

        when:
        OpenAPI oai = converter.convertToOpenApi(sd)
        println converter.openApiToYaml(oai)

        then:
        oai != null
        oai.getServers().size() == 1

    }

    void "test read Integer"() {
        ModelConverters modelConverters = new ModelConverters();

        when:
        def ss = modelConverters.read(NumberRange.Integer.class)

        then:
        ss.size() == 1
        ss.containsKey("Integer")

    }

}

