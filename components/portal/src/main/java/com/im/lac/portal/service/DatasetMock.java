package com.im.lac.portal.service;

import java.util.List;
import java.util.Map;

public class DatasetMock {

    private Long id;
    private List<DatasetRow> datasetRowList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<DatasetRow> getDatasetRowList() {
        return datasetRowList;
    }

    public void setDatasetRowList(List<DatasetRow> datasetRowList) {
        this.datasetRowList = datasetRowList;
    }

    public void addDatasetRow(Long id, Map properties) {
        DatasetRow datasetRow = new DatasetRow();
        datasetRow.setId(id);
        datasetRow.setProperties(properties);
        datasetRowList.add(datasetRow);
    }
}
