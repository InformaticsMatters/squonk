/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.chemaxon.db;

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
