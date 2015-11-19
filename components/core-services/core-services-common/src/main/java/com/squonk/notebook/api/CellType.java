package com.squonk.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class CellType implements Serializable {
    private String name;
    private String description;
    private Boolean executable;
    private final List<VariableDefinition> outputVariableDefinitionList = new ArrayList<>();
    private final List<String> optionNameList = new ArrayList<>();

    public List<VariableDefinition> getOutputVariableDefinitionList() {
        return outputVariableDefinitionList;
    }

    public List<String> getOptionNameList() {
        return optionNameList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getExecutable() {
        return executable;
    }

    public void setExecutable(Boolean executable) {
        this.executable = executable;
    }
}

