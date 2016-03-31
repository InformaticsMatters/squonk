package org.squonk.notebook.api;

import org.squonk.util.Utils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VariableKey implements Serializable {
    private Long cellId;
    private String variableName;

    public VariableKey() {}

    public VariableKey(Long cellId, String variableName) {
        this.cellId = cellId;
        this.variableName = variableName;
    }


    public Long getCellId() {
        return cellId;
    }


    public String getVariableName() {
        return variableName;
    }


    @Override
    public String toString() {
        return "VariableKey [CellId:" + cellId + " VariableName:" + variableName + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof VariableKey)) {
            return false;
        }
        return Utils.safeEquals(this.cellId, ((VariableKey)obj).cellId) && Utils.safeEquals(this.variableName, ((VariableKey)obj).variableName);
    }

    @Override
    public int hashCode() {
        return (cellId + variableName).hashCode();
    }
}
