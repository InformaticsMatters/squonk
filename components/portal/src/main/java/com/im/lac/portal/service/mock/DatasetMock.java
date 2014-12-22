package com.im.lac.portal.service.mock;

import com.im.lac.portal.service.api.DatasetRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasetMock {

    private Long id;
    private Map<Long, DatasetRow> datasetRowList = new HashMap<Long, DatasetRow>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<DatasetRow> getDatasetRowList() {
        return new ArrayList<DatasetRow>(datasetRowList.values());
    }

    public void addDatasetRow(Long id, DatasetRow datasetRow) {
        datasetRowList.put(id, datasetRow);
    }

    public DatasetRow findDatasetRowById(Long id) {
        return datasetRowList.get(id);
    }

}
