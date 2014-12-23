package com.im.lac.portal.service.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface DatasetService {

    DatasetDescriptor createDataset(DatamartSearch datamartSearch);

    DatasetDescriptor createDataset(DatasetInputStreamFormat format, InputStream inputStream, Map<String, Class> fieldConfig);

    List<DatasetDescriptor> listDatasetDescriptor(ListDatasetDescriptorFilter filter);

    List<DatasetRow> listDatasetRow(ListDatasetRowFilter filter);

    DatasetRow findDatasetRowById(Long datasetId, Long rowId);

    // metadata related methods

    DatasetDescriptor createDatasetDescriptor(DatasetDescriptor datasetDescriptor);

    DatasetRowDescriptor createDatasetRowDescriptor(DatasetDescriptor datasetDescriptor, DatasetRowDescriptor datasetRowDescriptor);

    void removeDatasetRowDescriptor(DatasetDescriptor datasetDescriptor, DatasetRowDescriptor datasetRowDescriptor);

    PropertyDefinition createPropertyDefinition(DatasetDescriptor datasetDescriptor, DatasetRowDescriptor datasetRowDescriptor, PropertyDefinition propertyDefinition);

    void removePropertyDefinition(DatasetDescriptor datasetDescriptor, DatasetRowDescriptor datasetRowDescriptor, PropertyDefinition propertyDefinition);

}
