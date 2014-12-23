package com.im.lac.portal.service.mock;

import com.im.lac.portal.service.api.DatasetDescriptor;
import com.im.lac.portal.service.api.DatasetRowDescriptor;
import com.im.lac.portal.service.api.PropertyDefinition;

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
        for (DatasetDescriptor dsd : datasetDescriptorList) {
            if (dsd.getId().equals(id)) {
                return dsd;
            }
        }
        return null;
    }

    public void removeDatasetDescriptor(Long datasetDescriptorId) {
        for (DatasetDescriptor datasetDescriptor : datasetDescriptorList) {
            if (datasetDescriptor.getId().equals(datasetDescriptorId)) {
                datasetDescriptorList.remove(datasetDescriptor);
                return;
            }
        }
    }

    public DatasetRowDescriptor persistDatasetRowDescriptor(DatasetRowDescriptor datasetRowDescriptor) {
        datasetRowDescriptor.setId(getNextId());
        return datasetRowDescriptor;
    }

    public void removeDatasetRowDescriptor(Long datasetDescriptorId, Long datasetRowDescriptorId) {
        DatasetDescriptor datasetDescriptor = findDatasetDescriptorById(datasetDescriptorId);
        for (DatasetRowDescriptor datasetRowDescriptor : datasetDescriptor.getDatasetRowDescriptorList()) {
            if (datasetRowDescriptor.getId().equals(datasetRowDescriptorId)) {
                datasetDescriptor.getDatasetRowDescriptorList().remove(datasetRowDescriptor);
                return;
            }
        }
    }

    public PropertyDefinition persistPropertyDefinition(PropertyDefinition propertyDefinition) {
        propertyDefinition.setId(getNextId());
        return propertyDefinition;
    }

    public void removePropertyDefinition(Long datasetDescriptorId, Long datasetRowDescriptorId, Long propertyDefinitionId) {
        DatasetDescriptor datasetDescriptor = findDatasetDescriptorById(datasetDescriptorId);
        for (DatasetRowDescriptor datasetRowDescriptor : datasetDescriptor.getDatasetRowDescriptorList()) {
            if (datasetRowDescriptor.getId().equals(datasetRowDescriptorId)) {
                for (PropertyDefinition propertyDefinition : datasetRowDescriptor.getPropertyDefinitionList()) {
                    if (propertyDefinition.getId().equals(propertyDefinitionId)) {
                        datasetRowDescriptor.removePropertyDefinition(propertyDefinition);
                        return;
                    }
                }
            }
        }
    }

    private Long getNextId() {
        id = id + 1;
        return id;
    }

}
