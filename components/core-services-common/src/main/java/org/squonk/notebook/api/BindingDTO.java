package org.squonk.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BindingDTO {
    private String name;
    private VariableKey variableKey;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VariableKey getVariableKey() {
        return variableKey;
    }

    public void setVariableKey(VariableKey variableKey) {
        this.variableKey = variableKey;
    }

    @Override
    public String toString() {
        return "BindingDTO [name:" + name + " variableKey:" + variableKey + "]";
    }
}
