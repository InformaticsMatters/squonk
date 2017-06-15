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

package org.squonk.notebook.api;

import org.squonk.util.Utils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VariableKey implements Serializable {
    private Long cellId;
    private String variableName;

    public VariableKey() {}

    public VariableKey(Long cellId, String variableName) {
        this.cellId = cellId;
        this.variableName = variableName;
    }


    public Long getCellId() {
        return cellId;
    }


    public String getVariableName() {
        return variableName;
    }


    @Override
    public String toString() {
        return "VariableKey [CellId:" + cellId + " VariableName:" + variableName + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof VariableKey)) {
            return false;
        }
        return Utils.safeEquals(this.cellId, ((VariableKey)obj).cellId) && Utils.safeEquals(this.variableName, ((VariableKey)obj).variableName);
    }

    @Override
    public int hashCode() {
        return (cellId + variableName).hashCode();
    }
}
