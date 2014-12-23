package com.im.lac.portal.service.mock;

import com.im.lac.portal.service.api.DatasetDescriptor;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DatabaseMock {

    private Long id = new Long("0");
    private List<DatasetMock> datasetMockList = new ArrayList<DatasetMock>();
    private List<DatasetDescriptor> datasetDescriptorList = new ArrayList<DatasetDescriptor>();

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

    public DatasetMock findDatasetMockById(Long id) {
        for (DatasetMock datasetMock : datasetMockList) {
            if (datasetMock.getId().equals(id)) {
                return datasetMock;
            }
        }
        return null;
    }

    public DatasetDescriptor persistDatasetDescriptor(DatasetDescriptor datasetDescriptor) {
        datasetDescriptor.setId(getNextId());
        this.datasetDescriptorList.add(datasetDescriptor);

        return datasetDescriptor;
    }

    public DatasetDescriptor findDatasetDescriptorById(Long id) {
        for (DatasetDescriptor datasetDescriptor : datasetDescriptorList) {
            if (datasetDescriptor.getId().equals(id)) {
                return datasetDescriptor;
            }
        }
        return null;
    }

    public void removeDatasetDescriptor(Long id) {
        for (DatasetDescriptor datasetDescriptor : datasetDescriptorList) {
            if (datasetDescriptor.getId().equals(id)) {
                datasetDescriptorList.remove(datasetDescriptor);
                return;
            }
        }
    }

    private Long getNextId() {
        id = id + 1;
        return id;
    }

}
