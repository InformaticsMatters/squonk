package com.im.lac.portal.service.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface DatasetService {

    DatasetDescriptor createDataset(DatamartSearch datamartSearch);

    DatasetDescriptor createDataset(DatasetInputStreamFormat format, InputStream inputStream, Map<String, Class> fieldConfig);

    List<DatasetDescriptor> listDatasetDescriptor(ListDatasetDescriptorFilter filter);

    List<DatasetRow> listDatasetRow(ListDatasetRowFilter filter);

    DatasetRow findDatasetRowById(Long datasetDescriptorId, Long rowId);

    // metadata related methods

    DatasetDescriptor createDatasetDescriptor(DatasetDescriptor datasetDescriptor);

    void removeDatasetDescriptor(Long datasetDescriptorId);

    DatasetRowDescriptor createDatasetRowDescriptor(Long datasetDescriptorId, DatasetRowDescriptor datasetRowDescriptor);

    void removeDatasetRowDescriptor(Long datasetDescriptorId, Long datasetRowDescriptorId);

    PropertyDefinition createPropertyDefinition(Long datasetDescriptorId, Long datasetRowDescriptorId, PropertyDefinition propertyDefinition);

    void removePropertyDefinition(Long datasetDescriptorId, Long datasetRowDescriptorId, Long propertyDefinitionId);

}
