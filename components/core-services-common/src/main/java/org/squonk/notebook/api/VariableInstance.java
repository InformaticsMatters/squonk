package org.squonk.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VariableInstance implements Serializable {
    private final static long serialVersionUID = 1l;
    private Long cellId;
    private VariableDefinition variableDefinition;
    private boolean dirty = false;

    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }

    public VariableDefinition getVariableDefinition() {
        return variableDefinition;
    }

    public void setVariableDefinition(VariableDefinition variableDefinition) {
        this.variableDefinition = variableDefinition;
    }

    public String calculateKey() {
        return cellId + "." + variableDefinition.getName();
    }

}
