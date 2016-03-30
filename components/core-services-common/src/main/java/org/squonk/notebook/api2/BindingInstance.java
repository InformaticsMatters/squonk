package org.squonk.notebook.api2;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class BindingInstance implements Serializable {
    private final static long serialVersionUID = 1l;
    private BindingDefinition bindingDefinition;
    private VariableInstance variableInstance;
    private boolean dirty = true;

    @JsonIgnore
    public String getName() {
        return bindingDefinition.getName();
    }

    @JsonIgnore
    public String getDisplayName() {
        return bindingDefinition.getDisplayName();
    }

    public VariableInstance getVariableInstance() {
        return variableInstance;
    }

    public void setVariableInstance(VariableInstance variableInstance) {
        dirty = true;
        this.variableInstance = variableInstance;
    }

    @JsonIgnore
    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }

    public BindingDefinition getBindingDefinition() {
        return bindingDefinition;
    }

    public void setBindingDefinition(BindingDefinition bindingDefinition) {
        this.bindingDefinition = bindingDefinition;
    }
}
