package com.im.lac.portal.service.mock;

import com.im.lac.portal.service.api.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DatasetMock {

    private Long id;
    private Map<Long, RowMock> rowMap = new HashMap<Long, RowMock>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Row> getRowList() {
        return new ArrayList<Row>(rowMap.values());
    }

    public void addRow(Long id, RowMock rowMock) {
        rowMap.put(id, rowMock);
    }

    public RowMock findRowById(Long id) {
        return rowMap.get(id);
    }

}
