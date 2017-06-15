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

package org.squonk.execution.variable;

import org.squonk.notebook.api.VariableKey;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author timbo
 */
public interface VariableLoader {

    public void save() throws IOException;
    
    public <V> V readFromText(VariableKey var, Class<V> type) throws IOException;
    
    public <V> V readFromJson(VariableKey var, Class<V> type) throws IOException;
    
    public InputStream readBytes(VariableKey var, String label) throws IOException;
    
    public void writeToText(VariableKey var, Object o) throws IOException;
    
    public void writeToJson(VariableKey var, Object o) throws IOException;
    
    public void writeToBytes(VariableKey var, String label, InputStream is) throws IOException;

}
