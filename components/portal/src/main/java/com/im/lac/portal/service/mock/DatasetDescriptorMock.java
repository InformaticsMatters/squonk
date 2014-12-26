package com.im.lac.portal.service.mock;

import com.im.lac.portal.service.api.DatasetDescriptor;
import com.im.lac.portal.service.api.RowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DatasetDescriptorMock implements DatasetDescriptor {

    private Long id;
    private String description;
    private Long datasetMockId;
    private Map<Long, RowDescriptorMock> rowDescriptorMap = new HashMap<Long, RowDescriptorMock>();

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<RowDescriptor> listAllRowDescriptors() {
        return new ArrayList<RowDescriptor>(rowDescriptorMap.values());
    }

    @Override
    public RowDescriptor findRowDescriptorById(Long id) {
        return rowDescriptorMap.get(id);
    }

    public Long getDatasetMockId() {
        return datasetMockId;
    }

    public void setDatasetMockId(Long datasetMockId) {
        this.datasetMockId = datasetMockId;
    }

    public void addRowDescriptor(RowDescriptorMock rowDescriptorMock) {
        rowDescriptorMap.put(rowDescriptorMock.getId(), rowDescriptorMock);
    }

    public void removeRowDescriptor(Long id) {
        rowDescriptorMap.remove(id);
    }

}
