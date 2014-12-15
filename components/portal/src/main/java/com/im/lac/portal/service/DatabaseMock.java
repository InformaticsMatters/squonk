package com.im.lac.portal.service;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class DatabaseMock {

    private List<DatasetMock> datasetMockList;

    public List<DatasetMock> getDatasetMockList() {
        return datasetMockList;
    }

    public void setDatasetMockList(List<DatasetMock> datasetMockList) {
        this.datasetMockList = datasetMockList;
    }
}
