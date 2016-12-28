package org.squonk.cdk.services;

import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.io.IOMultiplicity;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IORoute;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.MoleculeObject;

/**
 * Created by timbo on 14/11/16.
 */
public class CdkConverterServices {


    public static final ServiceDescriptor SERVICE_DESCRIPTOR_CONVERT_TO_SDF = createServiceDescriptor(
            "cdk.export.sdf", "SDF Export (CDK)", "Convert to SD file format using CDK",
            new String[]{"export", "sdf", "sdfile", "cdk"},
            null,
            "default_icon.png", "convert_to_sdf", null);

    static final ServiceDescriptor[] ALL = new ServiceDescriptor[]{
            SERVICE_DESCRIPTOR_CONVERT_TO_SDF
    };


    private static ServiceDescriptor createServiceDescriptor(String id, String name, String description, String[] tags, String resourceUrl, String icon, String endpoint, OptionDescriptor[] options) {

        return new ServiceDescriptor(
                id,
                name,
                description,
                tags,
                resourceUrl,
                icon,
                new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("input", IORoute.STREAM)},
                new IODescriptor[] {IODescriptors.createSDF("output", IORoute.STREAM)},
                options,
                StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME,
                endpoint
        );
    }

}
