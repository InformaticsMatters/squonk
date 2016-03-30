package org.squonk.notebook.api2;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VariableDefinition implements Serializable {
    private final static long serialVersionUID = 1l;
    private String name;
    private String displayName;
    private VariableType variableType;
    private Object defaultValue;

    public VariableDefinition() {
    }

    public VariableDefinition(String name, String displayName, VariableType variableType) {
        this.name = name;
        this.variableType = variableType;
        this.displayName = displayName;
    }

    public VariableDefinition(String name, String displayName, VariableType variableType, Object defaultValue) {
        this(name, displayName, variableType);
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VariableType getVariableType() {
        return this.variableType;
    }

    public void setVariableType(VariableType variableType) {
        this.variableType = variableType;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
