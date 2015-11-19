package com.squonk.notebook.client;


import com.squonk.notebook.api.AbstractClient;
import com.squonk.notebook.api.CellType;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

public class CellClient extends AbstractClient implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(CallbackClient.class.getName());
    @Inject
    private CellClientConfig config;

    public List<CellType> listCellType() {
        GenericType<List<CellType>> genericType = new GenericType<List<CellType>>() {
        };
        return newResourceBuilder("/listCellType").get(genericType);
    }

    public void executeCell(Long notebookId, String cellName) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("cellName", cellName);
        newResourceBuilder("/executeCell", queryParams).post();
    }

    public CellType retrieveCellType(String name) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("name", name);
        return newResourceBuilder("/retrieveCellType", queryParams).get(CellType.class);
    }

    @Override
    protected String getServiceBaseUri() {
        return config.getServiceBaseUri();
    }
}
