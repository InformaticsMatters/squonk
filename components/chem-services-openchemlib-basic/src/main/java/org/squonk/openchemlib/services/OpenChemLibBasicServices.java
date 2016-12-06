package org.squonk.openchemlib.services;

import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.MoleculeObject;

/**
 * Created by timbo on 14/11/16.
 */
public class OpenChemLibBasicServices {


    static final ServiceDescriptor SERVICE_DESCRIPTOR_VERIFY = createServiceDescriptor(
            "ocl.calculators.verify",
            "Verify structure (OCL)",
            "Verify that the molecules are valid according to OpenChemLib",
            new String[]{"verify", "openchemlib"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Verify+structure+%28OCL%29",
            "icons/properties_add.png",
            "verify",
            new OptionDescriptor[]{OptionDescriptor.IS_FILTER, OptionDescriptor.FILTER_MODE});

    static final ServiceDescriptor SERVICE_DESCRIPTOR_LOGP = createServiceDescriptor(
            "ocl.logp", "LogP (OpenChemLib)", "OpenChemLib LogP prediction",
            new String[]{"logp", "partitioning", "molecularproperties", "openchemlib"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/LogP+%28OpenChemLib%29",
            "icons/properties_add.png", "logp", null);

    static final ServiceDescriptor SERVICE_DESCRIPTOR_LOGS = createServiceDescriptor(
            "ocl.logs", "LogS (OpenChemLib)", "OpenChemLib Aqueous Solubility prediction",
            new String[]{"logs", "solubility", "molecularproperties", "openchemlib"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/LogS+%28OpenChemLib%29",
            "icons/properties_add.png", "logs", null);

    static final ServiceDescriptor SERVICE_DESCRIPTOR_PSA = createServiceDescriptor(
            "ocl.psa", "PSA (OpenChemLib)", "OpenChemLib Polar Surface Area prediction",
            new String[]{"psa", "tpsa", "molecularproperties", "openchemlib"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/PSA+%28OpenChemLib%29",
            "icons/properties_add.png", "psa", null);


    static final ServiceDescriptor[] ALL = new ServiceDescriptor[] {
            SERVICE_DESCRIPTOR_VERIFY,
            SERVICE_DESCRIPTOR_LOGP,
            SERVICE_DESCRIPTOR_LOGS,
            SERVICE_DESCRIPTOR_PSA
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
