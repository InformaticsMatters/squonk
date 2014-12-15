package com.im.lac.portal.service;

import java.util.List;

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
}
