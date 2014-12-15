package com.im.lac.portal.service;

import java.io.InputStream;
import java.util.List;

public interface PrototypeService {

    DatasetDescriptor createDataset(DatamartSearch dataMartSearch);

    DatasetDescriptor createDataset(String format, InputStream sdfInputStream);

    List<Long> listDatasetRowId(DatasetDescriptor datasetDescriptor);

    List<DatasetDescriptor> listDatasetDescriptor();

    List<PropertyDefinition> listPropertyDefinition(ListPropertyDefinitionFilter filter);

    List<DatasetRow> listDatasetRow(ListDatasetRowFilter filter);

}
