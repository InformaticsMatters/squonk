package com.im.lac.chemaxon.db;

import chemaxon.jchem.db.UpdateHandler;
import java.sql.SQLException;

/**
 *
 * @author timbo
 */
public class UpdateHandlerWorker {

    private final UpdateHandler updateHandler;

    UpdateHandlerWorker(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    protected Integer execute(String mol, Object... values) throws SQLException {
        return execute(mol, null, values);
    }

    protected Integer execute(String mol, Integer cdid, Object... values) throws SQLException {
        
        updateHandler.setStructure(mol);
        if (cdid != null) {
            updateHandler.setID(cdid);
        }
        int i = 0;
        for (Object val : values) {
            i++;
            updateHandler.setValueForAdditionalColumn(i, val);
        }
        int id = updateHandler.execute(true);
        return id;
    }
    
    public void close() throws SQLException {
        updateHandler.close();
    }

}
