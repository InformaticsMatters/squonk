package org.squonk.core.client.config;

import org.squonk.util.IOUtils;

import java.util.logging.Logger;

/**
 * Created by timbo on 13/03/16.
 */
public class SquonkClientConfig {

    private static final Logger LOG = Logger.getLogger(SquonkClientConfig.class.getName());

    public static final SquonkClientConfig INSTANCE = new SquonkClientConfig();

    private final String coreServicesBaseUrl, chemServicesBaseUrl;


    private SquonkClientConfig() {
        coreServicesBaseUrl = IOUtils.getConfiguration("SQUONK_SERVICES_CORE", "http://" + IOUtils.getDockerGateway() + ":8091/coreservices/rest/v1");
        //chemServicesBaseUrl = "http://" + IOUtils.getConfiguration("PRIVATE_HOST", "localhost") + ":8092/";
        chemServicesBaseUrl = "http://chemservices:8080/";
        LOG.info("Using core services base URL: " + coreServicesBaseUrl);
    }

    public String getCoreServicesBaseUrl() {
        return coreServicesBaseUrl;
    }

    public String getBasicChemServicesBaseUrl() {
        return chemServicesBaseUrl;
    }

    public String getBasicCdkChemServicesBaseUrl() {
        return chemServicesBaseUrl + "chem-services-cdk-basic/rest/v1";
    }

    public String getBasicChemaxonChemServicesBaseUrl() {
        return chemServicesBaseUrl + "chem-services-chemaxon-basic/rest/v1";
    }

    public String getBasicRDKitChemServicesBaseUrl() {
        return chemServicesBaseUrl + "chem-services-rdkit-basic/rest/v1";
    }

    public String getBasicOpenChemLibChemServicesBaseUrl() {
        return chemServicesBaseUrl + "chem-services-openchemlib-basic/rest/v1";
    }
}
