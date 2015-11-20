package com.squonk.notebook.client;

import com.squonk.notebook.client.CallbackClientConfig;
import javax.enterprise.inject.Alternative;

@Alternative
public class DefaultCallbackClientConfig implements CallbackClientConfig {
    @Override
    public String getBaseUri() {
        return "http://localhost:8080/ws/callback";
    }
}
