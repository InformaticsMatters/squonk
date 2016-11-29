package org.squonk.cdk.services;

import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.MoleculeObject;

/**
 * Created by timbo on 14/11/16.
 */
public class CdkBasicServices {

    static final ServiceDescriptor SERVICE_DESCRIPTOR_VERIFY = createServiceDescriptor(
            "cdk.calculators.verify",
            "Verify structure (CDK)",
            "Verify that the molecules are valid according to CDK",
            new String[]{"verify", "cdk"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Verify+structure+%28CDK%29",
            "icons/properties_add.png",
            "verify",
            new OptionDescriptor[]{OptionDescriptor.IS_FILTER, OptionDescriptor.FILTER_MODE});

    static final ServiceDescriptor SERVICE_DESCRIPTOR_LOGP = createServiceDescriptor(
            "cdk.logp", "LogP (CDK)", "LogP predictions for XLogP, ALogP and AMR using CDK",
            new String[]{"logp", "partitioning", "molecularproperties", "cdk"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/LogP+%28CDK%29",
            "icons/properties_add.png", "logp", null);

    static final ServiceDescriptor SERVICE_DESCRIPTOR_HBA_HBD = createServiceDescriptor(
            "cdk.donors_acceptors", "HBA & HBD (CDK)", "H-bond donor and acceptor counts using CDK",
            new String[]{"hbd", "donors", "hba", "acceptors", "topology", "molecularproperties", "cdk"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/HBA+%26+HBD+%28CDK%29",
            "icons/properties_add.png", "donors_acceptors", null);

    static final ServiceDescriptor SERVICE_DESCRIPTOR_WIENER_NUMBERS = createServiceDescriptor(
            "cdk.wiener_numbers", "Wiener Numbers (CDK)", "Wiener path and polarity numbers using CDK",
            new String[]{"wiener", "topology", "molecularproperties", "cdk"},
            "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Wiener+Numbers+%28CDK%29",
            "icons/properties_add.png", "wiener_numbers", null);

    static final ServiceDescriptor[] ALL = new ServiceDescriptor[] {
            SERVICE_DESCRIPTOR_VERIFY,
            SERVICE_DESCRIPTOR_LOGP,
            SERVICE_DESCRIPTOR_HBA_HBD,
            SERVICE_DESCRIPTOR_WIENER_NUMBERS
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
