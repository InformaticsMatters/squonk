package com.im.lac.portal.service;

import java.util.List;

public interface VisualizerService {

    DatasetDescriptor createDataset(Search search);

    List<Long> openDataset(DatasetDescriptor datasetDescriptor);

}
