package com.im.lac.portal.service;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DatabaseMock {

    private Long id = new Long("1");
    private List<DatasetMock> datasetMockList = new ArrayList<DatasetMock>();

    public List<DatasetMock> getDatasetMockList() {
        return datasetMockList;
    }

    public DatasetDescriptor persistDatasetMock(DatasetMock datasetMock) {
        datasetMock.setId(getNextId());
        this.datasetMockList.add(datasetMock);

        DatasetDescriptor datasetDescriptor = new DatasetDescriptor();
        datasetDescriptor.setId(datasetMock.getId());
        return datasetDescriptor;
    }

    private Long getNextId() {
        id = id + 1;
        return id;
    }

}
