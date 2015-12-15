package org.squonk.notebook.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class NotebookDTO {
    private final List<CellDTO> cellList = new ArrayList<>();

    @XmlElement
    public List<CellDTO> getCellList() {
        return cellList;
    }

    public CellDTO findCell(String cellName) {
        for (CellDTO cellDTO : cellList) {
            if (cellDTO.getName().equals(cellName)) {
                return cellDTO;
            }
        }
        return null;
    }

}
