package org.squonk.notebook.client;

import org.squonk.util.IOUtils;
import javax.enterprise.inject.Default;

@Default
public class DefaultCellClientConfig implements CellClientConfig {
    
    String url = IOUtils.getConfiguration("SERVICE_CELL_EXECUTION", "http://localhost:8080/ws/cell");

    @Override
    public String getServiceBaseUri() {
        return url;
    }
    
}
