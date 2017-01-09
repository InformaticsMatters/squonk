package org.squonk.cdk.services;

import org.squonk.core.HttpServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;

/**
 * Created by timbo on 14/11/16.
 */
public class CdkConverterServices {


    public static final HttpServiceDescriptor SERVICE_DESCRIPTOR_CONVERT_TO_SDF = createServiceDescriptor(
            "cdk.export.sdf", "SDF Export (CDK)", "Convert to SD file format using CDK",
            new String[]{"export", "sdf", "sdfile", "cdk"},
            null,
            "default_icon.png", "convert_to_sdf", null);

    static final HttpServiceDescriptor[] ALL = new HttpServiceDescriptor[]{
            SERVICE_DESCRIPTOR_CONVERT_TO_SDF
    };


    private static HttpServiceDescriptor createServiceDescriptor(String id, String name, String description, String[] tags, String resourceUrl, String icon, String endpoint, OptionDescriptor[] options) {

        return new HttpServiceDescriptor(
                id,
                name,
                description,
                tags,
                resourceUrl,
                icon,
                new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("input")},
                new IODescriptor[] {IODescriptors.createSDF("output")},
                options,
                StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME,
                endpoint
        );
    }

}
