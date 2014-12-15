package com.im.lac.portal.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;

@ApplicationScoped
public class PrototypeServiceMock implements PrototypeService {

    @Inject
    private DatabaseMock databaseMock;

    @Override
    public DatasetDescriptor createDataset(DatamartSearch dataMartSearch) {
        return null;
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
