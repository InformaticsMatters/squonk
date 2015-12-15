package org.squonk.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class BindingDefinition implements Serializable {
    private String name;
    private String displayName;
    private final List<VariableType> acceptedVariableTypeList = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<VariableType> getAcceptedVariableTypeList() {
        return acceptedVariableTypeList;
    }
}

