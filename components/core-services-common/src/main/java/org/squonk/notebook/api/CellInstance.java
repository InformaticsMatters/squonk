package org.squonk.notebook.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlRootElement
public class CellInstance implements Serializable {
    private final static long serialVersionUID = 1l;

    private CellDefinition cellDefinition;
    private final Map<String, BindingInstance> bindingInstanceMap = new LinkedHashMap<>();
    private final Map<String, VariableInstance> variableInstanceMap = new LinkedHashMap<>();
    private final Map<String, OptionInstance> optionInstanceMap = new LinkedHashMap<>();
    private Long id;
    private String name;
    private int positionLeft;
    private int positionTop;
    private int sizeWidth;
    private int sizeHeight;
    private boolean dirty = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CellDefinition getCellDefinition() {
        return cellDefinition;
    }

    public void setCellDefinition(CellDefinition cellDefinition) {
        this.cellDefinition = cellDefinition;
    }

    public Map<String, BindingInstance> getBindingInstanceMap() {
        return bindingInstanceMap;
    }

    public Map<String, VariableInstance> getVariableInstanceMap() {
        return variableInstanceMap;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, OptionInstance> getOptionInstanceMap() {
        return optionInstanceMap;
    }

    public int getPositionLeft() {
        return positionLeft;
    }

    public void setPositionLeft(int positionLeft) {
        dirty = true;
        this.positionLeft = positionLeft;
    }

    public int getPositionTop() {
        return positionTop;
    }

    public void setPositionTop(int positionTop) {
        dirty = true;
        this.positionTop = positionTop;
    }

    public int getSizeWidth() {
        return sizeWidth;
    }

    public void setSizeWidth(int sizeWidth) {
        dirty = true;
        this.sizeWidth = sizeWidth;
    }

    public int getSizeHeight() {
        return sizeHeight;
    }

    public void setSizeHeight(int sizeHeight) {
        dirty = true;
        this.sizeHeight = sizeHeight;
    }

    @JsonIgnore
    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
        for (VariableInstance variableInstance : variableInstanceMap.values()) {
            variableInstance.resetDirty();
        }
        for (OptionInstance optionInstance : optionInstanceMap.values()) {
            optionInstance.resetDirty();
        }
        for (BindingInstance bindingInstance : bindingInstanceMap.values()) {
            bindingInstance.resetDirty();
        }
    }
}
