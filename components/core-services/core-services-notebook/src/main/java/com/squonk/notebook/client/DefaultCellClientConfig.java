package com.squonk.notebook.client;

import com.squonk.notebook.client.CellClientConfig;
import javax.enterprise.inject.Alternative;

@Alternative
public class DefaultCellClientConfig implements CellClientConfig {
    @Override
    public String getServiceBaseUri() {
        return "http://localhost:8080/ws/cell";
    }
}
