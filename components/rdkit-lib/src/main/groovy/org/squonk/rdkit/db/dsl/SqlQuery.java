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

package org.squonk.rdkit.db.dsl;

import org.squonk.rdkit.db.FingerprintType;
import org.squonk.rdkit.db.RDKitTable;
import org.squonk.rdkit.db.RDKitTableLoader;

import java.util.*;

/**
 * Created by timbo on 13/12/2015.
 */
public class SqlQuery {

    protected RDKitTable rdkTable;
    protected IConfiguration config;


    public SqlQuery(RDKitTable rdkTable, IConfiguration config) {
        this.rdkTable = rdkTable;
        this.config = config;
    }

    public SqlQuery(RDKitTable rdkTable) {
       this(rdkTable, null);
    }

    public List<FingerprintType> getFingerPrintTypes() {
        return Collections.unmodifiableList(rdkTable.getFingerprintTypes());
    }

    List<Column> getColumns() {
        return rdkTable.columns;
    }

    public Select select(Column... cols) {
        return cols.length == 0 ? new Select(this, rdkTable.getColumns()) : new Select(this, cols);
    }

    public Select select(List<Column> cols) {
        return new Select(this, cols);
    }

    public RDKitTableLoader loader() {
        return new RDKitTableLoader(rdkTable, config);
    }

    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }



}
