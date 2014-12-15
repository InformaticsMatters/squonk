package com.im.lac.portal.service;

import java.util.List;

public interface VisualizerService {

    DatasetDescriptor createDataset(DataMartSearch dataMartSearch);

    List<Long> openDataset(DatasetDescriptor datasetDescriptor);

    List<DatasetDescriptor> listDatasetDescriptor();

    List<PropertyDefinition> listPropertyDefinition(ListPropertyDefinitionFilter filter);

}
