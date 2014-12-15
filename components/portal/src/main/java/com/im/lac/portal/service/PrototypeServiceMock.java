package com.im.lac.portal.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PrototypeServiceMock implements PrototypeService {

    @Inject
    private DatabaseMock databaseMock;

    @Override
    public DatasetDescriptor createDataset(DatamartSearch dataMartSearch) {
        DatasetMock datasetMock = new DatasetMock();

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("structure", "Structure");
        properties.put("p1", "Property 1");
        properties.put("p2", "Property 2");
        for (int i = 1; i <= 100; i++) {
            datasetMock.addDatasetRow(new Long(i), properties);
        }

        databaseMock.persistDatasetMock(datasetMock);

        DatasetDescriptor datasetDescriptor = new DatasetDescriptor();
        datasetDescriptor.setId(datasetMock.getId());
        return datasetDescriptor;
    }

    @Override
    public DatasetDescriptor createDataset(DatasetInputStreamFormat format, InputStream inputStream) {
        // import from SDF and add to database mock.
        return null;
    }

    @Override
    public List<Long> listDatasetRowId(DatasetDescriptor datasetDescriptor) {
        return null;
    }

    @Override
    public List<DatasetDescriptor> listDatasetDescriptor() {
        return null;
    }

    @Override
    public List<PropertyDefinition> listPropertyDefinition(ListPropertyDefinitionFilter filter) {
        return null;
    }

    @Override
    public List<DatasetRow> listDatasetRow(ListDatasetRowFilter filter) {
        return null;
    }

}
