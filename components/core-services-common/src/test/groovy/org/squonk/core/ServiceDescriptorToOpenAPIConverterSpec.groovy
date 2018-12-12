package org.squonk.core

import io.swagger.v3.oas.models.OpenAPI
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.io.IODescriptors
import org.squonk.options.OptionDescriptor
import spock.lang.Specification

class ServiceDescriptorToOpenAPIConverterSpec extends Specification {

    void "to openapi"() {

        def converter = new ServiceDescriptorToOpenAPIConverter("http://squonk.it/jobexecutor/rest/v1/jobs")
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

}

