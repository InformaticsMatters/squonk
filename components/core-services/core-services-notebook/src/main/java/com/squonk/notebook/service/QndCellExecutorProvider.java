package com.squonk.notebook.service;

import com.squonk.notebook.execution.QndCellExecutor;
import com.squonk.notebook.api.CellType;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.Serializable;

public class QndCellExecutorProvider implements Serializable {
    @Inject
    private Instance<QndCellExecutor> cellHandlerInstance;

    public QndCellExecutor resolveCellHandler(CellType cellType) {

        for (QndCellExecutor qndCellExecutor : cellHandlerInstance) {
            if (qndCellExecutor.handles(cellType)) {
                return qndCellExecutor;
            }
        }
        return null;
    }

}
