package org.squonk.notebook.api2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@XmlRootElement
public class NotebookInstance implements Serializable {
    private final static long serialVersionUID = 1l;

    private static final Logger LOG = Logger.getLogger(NotebookInstance.class.getName());
    private final List<Long> removedCellIdList = new ArrayList<>();
    private final List<CellInstance> cellInstanceList = new ArrayList<>();
    private Long lastCellId;


    public NotebookInstance() {

    }

    public NotebookInstance(@JsonProperty("lastCellId") Long lastCellId) {
        this.lastCellId = lastCellId;
    }

    public List<CellInstance> getCellInstanceList() {
        return cellInstanceList;
    }

    public VariableInstance findVariableByCellName(String producerName, String name) {
        for (CellInstance cell : cellInstanceList) {
            if (cell.getName().equals(producerName)) {
                return cell.getVariableInstanceMap().get(name);
            }
        }
        return null;
    }

    public VariableInstance findVariableByCellId(Long producerId, String name) {
        for (CellInstance cell : cellInstanceList) {
            if (cell.getId().equals(producerId)) {
                return cell.getVariableInstanceMap().get(name);
            }
        }
        return null;
    }

    public CellInstance addCellInstance(CellDefinition cellType) {
        CellInstance cell = createCellInstance(cellType);
        cell.setName(calculateCellName(cell));
        cellInstanceList.add(cell);
        return cell;
    }

    private String calculateCellName(CellInstance cell) {
        int typeCount = 0;
        Set<String> nameSet = new HashSet<String>();
        for (CellInstance item : cellInstanceList) {
            if (item.getCellDefinition().equals(cell.getCellDefinition())) {
                typeCount++;
            }
            nameSet.add(item.getName());
        }
        int suffix = typeCount + 1;
        String newName = cell.getCellDefinition().getName() + suffix;
        while (nameSet.contains(newName)) {
            suffix++;
            newName = cell.getCellDefinition().getName() + suffix;
        }
        return newName;
    }

    public CellInstance findCellInstanceByName(String name) {
        for (CellInstance cell : cellInstanceList) {
            if (cell.getName().equals(name)) {
                return cell;
            }
        }
        return null;
    }

    public CellInstance findCellInstanceById(Long id) {
        for (CellInstance cell : cellInstanceList) {
            if (cell.getId().equals(id)) {
                return cell;
            }
        }
        return null;
    }

    private CellInstance createCellInstance(CellDefinition cellDefinition) {
        CellInstance cell = new CellInstance();
        cell.setCellDefinition(cellDefinition);
        cell.setId(lastCellId == null ? 1L : lastCellId + 1L);
        lastCellId = cell.getId();
        for (VariableDefinition variableDefinition : cellDefinition.getVariableDefinitionList()) {
            VariableInstance variable = new VariableInstance();
            variable.setVariableDefinition(variableDefinition);
            variable.setCellId(cell.getId());
            cell.getVariableInstanceMap().put(variableDefinition.getName(), variable);
        }
        for (BindingDefinition bindingDefinition : cellDefinition.getBindingDefinitionList()) {
            BindingInstance binding = new BindingInstance();
            binding.setBindingDefinition(bindingDefinition);
            cell.getBindingInstanceMap().put(bindingDefinition.getName(), binding);
        }
        for (OptionDescriptor optionDescriptor : cellDefinition.getOptionDefinitionList()) {
            OptionInstance option = new OptionInstance();
            option.setOptionDescriptor(optionDescriptor);
            cell.getOptionInstanceMap().put(optionDescriptor.getName(), option);
        }
        return cell;
    }

    public void removeCellInstance(Long id) {
        CellInstance cellInstance = findCellInstanceById(id);
        cellInstanceList.remove(cellInstance);
        removedCellIdList.add(id);
        for (CellInstance otherCellInstance : cellInstanceList) {
            for (BindingInstance bindingInstance : otherCellInstance.getBindingInstanceMap().values()) {
                VariableInstance variableInstance = bindingInstance.getVariableInstance();
                if (variableInstance != null && variableInstance.getCellId().equals(id)) {
                    bindingInstance.setVariableInstance(null);
                }
            }
        }
    }

    public void applyChangesFrom(NotebookInstance notebookInstance) {
        for (Long cellId : notebookInstance.removedCellIdList) {
            removeCellInstance(cellId);
        }
        for (CellInstance cellInstance : notebookInstance.cellInstanceList) {
            CellInstance localCellInstance = findCellInstanceById(cellInstance.getId());
            if (localCellInstance == null) {
                cellInstanceList.add(cellInstance);
                lastCellId = cellInstance.getId();
            }  else {
                applyCellChanges(cellInstance, localCellInstance);
            }
        }
    }

    private void applyCellChanges(CellInstance cellInstance, CellInstance localCellInstance) {
        if (cellInstance.isDirty()) {
           localCellInstance.setPositionLeft(cellInstance.getPositionLeft());
           localCellInstance.setPositionTop(cellInstance.getPositionTop());
           localCellInstance.setSizeHeight(cellInstance.getSizeHeight());
           localCellInstance.setSizeWidth(cellInstance.getSizeWidth());
        }
        for (OptionInstance optionInstance : cellInstance.getOptionInstanceMap().values()) {
            if (optionInstance.isDirty()) {
                localCellInstance.getOptionInstanceMap().get(optionInstance.getOptionDescriptor().getName()).setValue(optionInstance.getValue());
            }
        }
        for (BindingInstance bindingInstance : cellInstance.getBindingInstanceMap().values()) {
            if (bindingInstance.isDirty()) {
                if (bindingInstance.getVariableInstance() == null) {
                    localCellInstance.getBindingInstanceMap().get(bindingInstance.getName()).setVariableInstance(null);
                } else {
                    VariableInstance variableInstance = findVariableByCellId(bindingInstance.getVariableInstance().getCellId(), bindingInstance.getVariableInstance().getVariableDefinition().getName());
                    localCellInstance.getBindingInstanceMap().get(bindingInstance.getName()).setVariableInstance(variableInstance);
                }
            }
        }
    }

    public void resetDirty() {
        removedCellIdList.clear();
        for (CellInstance cellInstance : cellInstanceList) {
            cellInstance.resetDirty();
        }
    }

    public static NotebookInstance fromJsonString(String string) throws Exception {
        if (string == null) {
            return null;
        } else {
            NotebookInstance notebookInstance = new ObjectMapper().readValue(string, NotebookInstance.class);
            notebookInstance.fixReferences();
            return notebookInstance;
        }
    }

    protected void fixReferences() {
        for (CellInstance cellInstance : cellInstanceList) {
            for (BindingInstance bindingInstance : cellInstance.getBindingInstanceMap().values()) {
                VariableInstance variableInstance = bindingInstance.getVariableInstance();
                 if (variableInstance != null) {
                     bindingInstance.setVariableInstance(findVariableByCellId(variableInstance.getCellId(), variableInstance.getVariableDefinition().getName()));
                 }
            }
        }

    }

    public String toJsonString() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(byteArrayOutputStream, this);
        byteArrayOutputStream.flush();
        return new String(byteArrayOutputStream.toByteArray());
    }

    public Long getLastCellId() {
        return lastCellId;
    }


}
