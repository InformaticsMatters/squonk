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

import java.util.List;

/**
 * Created by timbo on 14/12/2015.
 */
public final class Column implements IProjectionPart {

    final String name;
    final String type;
    final String definition;

    final Table table;

    Column(Table table, String name, String type, String definition) {
        this.table = table;
        this.name = name;
        this.type = type;
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public String getProjectionName() {
        return name;
    }

    public boolean isSameAs(Column col) {
        if (col != null) {
            return (this.name.equals(col.name) && this.table.getBaseName().equals(col.table.getBaseName()));
        }
        return false;
    }

    public int appendToProjections(StringBuilder builder, List bindVars) {
        builder.append(table.aliasOrSchemaPlusTable())
                .append(".")
                .append(name);
        return 1;
    }

}
