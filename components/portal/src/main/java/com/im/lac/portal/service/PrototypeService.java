package com.im.lac.portal.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface PrototypeService {

    DatasetDescriptor createDataset(DatamartSearch datamartSearch);

    DatasetDescriptor createDataset(DatasetInputStreamFormat format, InputStream inputStream, Map<String, Class> fieldConfig);

    List<DatasetDescriptor> listDatasetDescriptor(ListDatasetDescriptorFilter filter);

    List<DatasetRow> listDatasetRow(ListDatasetRowFilter filter);

    DatasetRow findDatasetRowById(Long datasetId, Long rowId);

}
