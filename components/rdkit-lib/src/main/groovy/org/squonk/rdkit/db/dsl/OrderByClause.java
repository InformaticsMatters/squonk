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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by timbo on 14/12/2015.
 */
public class OrderByClause {

    final Select select;
    final List<IOrderByPart> parts = new ArrayList<>();

    OrderByClause(Select select) {
        this.select = select;
    }

    void add(Column col, boolean ascending) {
        add(new OrderByPart(this, col, ascending));
    }

    void add(IOrderByPart orderBy) {
        parts.add(orderBy);
    }

    void appendToOrderBy(StringBuilder buf) {
        int count = 0;
        for (IOrderByPart part : parts) {
            if (count == 0) {
                buf.append("\n  ORDER BY ");
            } else {
                buf.append(",");
            }
            count += part.appendToOrderBy(buf);
        }

    }
}
