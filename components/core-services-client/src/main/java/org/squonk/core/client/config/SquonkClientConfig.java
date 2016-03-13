package org.squonk.core.client.config;

import org.squonk.util.IOUtils;

import java.util.logging.Logger;

/**
 * Created by timbo on 13/03/16.
 */
public class SquonkClientConfig {

    private static final Logger LOG = Logger.getLogger(SquonkClientConfig.class.getName());

    public static final SquonkClientConfig INSTANCE = new SquonkClientConfig();

    private final String coreServiceBaseUrl;


    private SquonkClientConfig() {
        coreServiceBaseUrl = IOUtils.getConfiguration("SQUONK_SERVICES_CORE", "http://localhost:8091/coreservices/rest/v1");
        LOG.info("Using core services base URL: " + coreServiceBaseUrl);

    }

    public String getCoreServiceBaseUrl() {
        return coreServiceBaseUrl;
    }
}
