package com.squonk.notebook.client;

import com.squonk.util.IOUtils;
import javax.enterprise.inject.Default;

@Default
public class DefaultCallbackClientConfig implements CallbackClientConfig {

    String url = IOUtils.getConfiguration("SERVICE_CALLBACK", "http://localhost:8080/ws/callback");

    @Override
    public String getBaseUri() {
        return url;
    }
}
