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
    private final Map<String, BindingDTO> bindingMap = new HashMap<>();
    private final List<String> outputVariableNameList = new ArrayList<>();
    private final Map<String, OptionDTO> optionMap = new HashMap<>();
    private int positionTop;
    private int positionLeft;

    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    @XmlElement
    public Map<String, BindingDTO> getBindingMap() {
        return bindingMap;
    }

    @XmlElement
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

    @XmlElement
    public Map<String, OptionDTO> getOptionMap() {
        return optionMap;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
