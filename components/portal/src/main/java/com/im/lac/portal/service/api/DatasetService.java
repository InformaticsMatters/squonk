package com.im.lac.portal.service.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface DatasetService {

    DatasetDescriptor importFromStream(DatasetInputStreamFormat format, InputStream inputStream, Map<String, Class> fieldConfig);

    List<DatasetDescriptor> listDatasetDescriptor(ListDatasetDescriptorFilter filter);

    List<Row> listRow(ListRowFilter filter);

    Row findRowById(Long datasetDescriptorId, Long rowId);

    DatasetDescriptor createDatasetDescriptor(DatasetDescriptor datasetDescriptor);

    void removeDatasetDescriptor(Long datasetDescriptorId);

    RowDescriptor createRowDescriptor(Long datasetDescriptorId, RowDescriptor rowDescriptor);

    void removeRowDescriptor(Long datasetDescriptorId, Long rowDescriptorId);

    PropertyDescriptor createPropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, PropertyDescriptor propertyDescriptor);

    void removePropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, Long propertyDescriptorId);

}
