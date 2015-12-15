package org.squonk.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class CellType implements Serializable {
    private String name;
    private String description;
    private Boolean executable;
    private final List<BindingDefinition> bindingDefinitionList = new ArrayList<>();
    private final List<VariableDefinition> outputVariableDefinitionList = new ArrayList<>();
    private final List<OptionDefinition> optionDefinitionList = new ArrayList<>();

    public CellType(String name, String description, Boolean executable) {
        this.name = name;
        this.description = description;
        this.executable = executable;
    }

    public CellType() {

    }

    public CellType withOutputVariable(String name, VariableType variableType, Object defaultValue) {
        outputVariableDefinitionList.add(new VariableDefinition(name, variableType, defaultValue));
        return this;
    }

    public CellType withOutputVariable(String name, VariableType variableType) {
        outputVariableDefinitionList.add(new VariableDefinition(name, variableType));
        return this;
    }


    public List<BindingDefinition> getBindingDefinitionList() {
        return bindingDefinitionList;
    }


    public List<VariableDefinition> getOutputVariableDefinitionList() {
        return outputVariableDefinitionList;
    }

    public List<OptionDefinition> getOptionDefinitionList() {
        return optionDefinitionList;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CellType withOption(String name, OptionType optionType) {
        optionDefinitionList.add(new OptionDefinition(name, optionType));
        return this;
    }
}
