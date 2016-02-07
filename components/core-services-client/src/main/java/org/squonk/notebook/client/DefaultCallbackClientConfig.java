package org.squonk.notebook.client;

import org.squonk.util.IOUtils;
import javax.enterprise.inject.Default;
import java.util.logging.Logger;

@Default
public class DefaultCallbackClientConfig implements CallbackClientConfig {

    private static final Logger LOG = Logger.getLogger(DefaultCallbackClientConfig.class.getName());

    String url = IOUtils.getConfiguration("SERVICE_CALLBACK", "http://localhost:8080/ws/callback");

    public DefaultCallbackClientConfig() {
        LOG.info("Using base URL of " + url);
    }

    @Override
    public String getBaseUri() {
        return url;
    }
}
