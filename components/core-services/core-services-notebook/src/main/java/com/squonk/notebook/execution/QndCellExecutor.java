package com.squonk.notebook.execution;

import com.squonk.notebook.api.CellType;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface QndCellExecutor {

    void execute(String cellName);
    boolean handles(CellType cellType);
}
