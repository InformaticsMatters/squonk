package com.im.lac.portal.service.api;

import java.util.List;

public class ListRowFilter {

    private Long datasetid;
    private List<Long> rowIdList;

    public Long getDatasetId() {
        return datasetid;
    }

    public void setDatasetId(Long datasetid) {
        this.datasetid = datasetid;
    }

    public List<Long> getRowIdList() {
        return rowIdList;
    }

    public void setRowIdList(List<Long> rowIdList) {
        this.rowIdList = rowIdList;
    }
}
