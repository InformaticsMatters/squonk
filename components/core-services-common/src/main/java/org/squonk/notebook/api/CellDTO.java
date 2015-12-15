package org.squonk.notebook.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class CellDTO {
    private String name;
    private CellType cellType;
    private final List<VariableDTO> inputVariableList = new ArrayList<>();
    private final List<String> outputVariableNameList = new ArrayList<>();
    private final Map<String, Object> propertyMap = new HashMap<>();
    private int positionTop;
    private int positionLeft;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    @XmlElement
    public List<VariableDTO> getInputVariableList() {
        return inputVariableList;
    }

    @XmlElement
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public int getPositionTop() {
        return positionTop;
    }

    public void setPositionTop(int positionTop) {
        this.positionTop = positionTop;
    }

    public int getPositionLeft() {
        return positionLeft;
    }

    public void setPositionLeft(int positionLeft) {
        this.positionLeft = positionLeft;
    }
}
