package org.squonk.cdk.services;

import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.MoleculeObject;

/**
 * Created by timbo on 14/11/16.
 */
public class CdkConverterServices  {


    public static final ServiceDescriptor SERVICE_DESCRIPTOR_CONVERT_TO_SDF = createServiceDescriptor(
            "cdk.export.sdf", "SDF Export (CDK)", "Convert to SD file format using CDK",
            new String[]{"export", "sdf", "sdfile", "cdk"},
            null,
            "default_icon.png", "convert_to_sdf", null);

    static final ServiceDescriptor[] ALL = new ServiceDescriptor[] {
            SERVICE_DESCRIPTOR_CONVERT_TO_SDF
    };


    private static ServiceDescriptor createServiceDescriptor(String id, String name, String description, String[] tags, String resourceUrl, String icon, String endpoint, OptionDescriptor[] options) {

        return new ServiceDescriptor(
                id,
                name,
                description,
                tags,
                resourceUrl,
                MoleculeObject.class, // inputClass
                MoleculeObject.class, // outputClass
                ServiceDescriptor.DataType.STREAM, // inputType
                ServiceDescriptor.DataType.STREAM, // outputType
                icon,
                endpoint,
                true, // a relative URL
                options,
                StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME
        );
    }

}
