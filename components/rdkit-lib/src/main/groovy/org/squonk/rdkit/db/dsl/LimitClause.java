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

import org.squonk.types.MoleculeObject;

import java.util.List;

/**
 * Created by timbo on 13/12/2015.
 */
public class LimitClause implements IExecutable {

    final Select select;
    final int limit;

    LimitClause(Select select, int limit) {
        this.select = select;
        this.limit = limit;
    }

    void append(StringBuilder buf) {
        if (limit > 0) {
            buf.append("\n  LIMIT ").append(limit);
        }
    }

    public Select select() {
        return select;
    }

    @Override
    public List<MoleculeObject> execute() {
        return select.execute();
    }

}
