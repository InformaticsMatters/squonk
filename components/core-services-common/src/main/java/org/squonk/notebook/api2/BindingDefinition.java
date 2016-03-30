package org.squonk.notebook.api2;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XmlRootElement
public class BindingDefinition implements Serializable {
    private final static long serialVersionUID = 1l;
    private String name;
    private String displayName;
    private final List<VariableType> acceptedVariableTypeList = new ArrayList<>();

    public BindingDefinition() {
    }

    public BindingDefinition(String name, String displayName, VariableType... acceptedVariableTypes) {
        this.name = name;
        this.displayName = displayName;
        if (acceptedVariableTypes != null) {
            this.acceptedVariableTypeList.addAll(Arrays.asList(acceptedVariableTypes));
        }
    }



    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<VariableType> getAcceptedVariableTypeList() {
        return this.acceptedVariableTypeList;
    }
}
