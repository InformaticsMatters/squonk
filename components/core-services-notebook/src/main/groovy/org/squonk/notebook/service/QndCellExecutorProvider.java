package org.squonk.notebook.service;

import org.squonk.notebook.execution.QndCellExecutor;
import org.squonk.notebook.api.CellType;

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
