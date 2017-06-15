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

import chemaxon.jchem.db.StructureTableOptions;
import chemaxon.jchem.db.UpdateHandler;
import chemaxon.util.ConnectionHandler;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class UpdateHandlerSupport extends ConnectionHandlerSupport {

    private static final Logger LOG = Logger.getLogger(UpdateHandlerSupport.class.getName());

    public void createStructureTable(String tableName, int tableType, String standardizer, String extraColumnDefs) throws SQLException {
        StructureTableOptions opts = new StructureTableOptions(tableName, tableType);
        if (extraColumnDefs != null) {
            opts.setExtraColumnDefinitions(extraColumnDefs);
        }
        if (standardizer != null) {
            opts.setStandardizerConfig(standardizer);
        }
        createStructureTable(opts);
    }

    public void createStructureTable(StructureTableOptions opts) throws SQLException {
        ConnectionHandler conh = getConnectionHandler();
        UpdateHandler.createStructureTable(conh, opts);
    }

    public boolean hasStructureTable(String tableName) throws SQLException {
        ConnectionHandler conh = getConnectionHandler();
        return UpdateHandler.isStructureTable(conh, tableName);
    }

    public void dropStructureTable(String tableName, boolean failIfNotPresent) throws SQLException {
        ConnectionHandler conh = getConnectionHandler();
        if (UpdateHandler.isStructureTable(conh, tableName)) {
            UpdateHandler.dropStructureTable(conh, tableName);
        } else {
            if (failIfNotPresent) {
                throw new IllegalStateException("No such structure table");
            }
        }
    }

    public UpdateHandlerWorker createWorker(int mode, String tableName, String additionalColumns)
            throws SQLException {
        if (additionalColumns == null) {
            additionalColumns = "";
        }
        UpdateHandler uh = new UpdateHandler(getConnectionHandler(), mode, tableName, additionalColumns);
        return new UpdateHandlerWorker(uh);
    }

}
