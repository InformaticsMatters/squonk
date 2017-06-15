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

/**
 * Created by timbo on 14/12/2015.
 */
public class OrderByPart implements IOrderByPart {

    final OrderByClause orderByClause;
    final Column col;
    final boolean ascending;


    OrderByPart(OrderByClause orderByClause, Column col, boolean ascending) {
        this.orderByClause = orderByClause;
        this.col = col;
        this.ascending = ascending;
    }

    public int appendToOrderBy(StringBuilder buf) {
        buf.append(col.table.name).append(".").append(col.name).append(" ").append(ascending ? "ASC" : "DESC");
        return 1;
    }
}
