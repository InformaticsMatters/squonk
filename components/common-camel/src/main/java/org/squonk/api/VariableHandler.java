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

package org.squonk.api;

import java.io.IOException;
import java.io.InputStream;

/** Interfaces for a class that wants to define how it will be persisted using the Notebook/Variable API.
 * To conform the class must:
 * <ol>
 *     <li>Implement this interface</li>
 *     <li>Have a constructor with a single parameter of {@link ReadContext} or, for handling generic types, two parameters
 *     of types {java.lang.Class} and {@link ReadContext}</li>
 * </ol>
 * This allows the implementation to define exactly how it is persisted, using text and stream values.
 * For an example see {@link org.squonk.dataset.Dataset} which needs to persist 2 values separately, one for the metadata
 * and one for the actual (potentially very large) stream of data.
 *
 * Created by timbo on 13/03/16.
 */
public interface VariableHandler<T> {

    Class<T> getType();

    /** Write the variable using the Notebook/Variable API
     *
     * @param value The value to be writen
     * @param context The  WriteContext to write the variable to
     */
    void writeVariable(T value, WriteContext context) throws Exception;


    /** Read the variable using the Notebook/Variable API
     *
     * @param context
     * @return
     * @throws IOException
     */
    T readVariable(ReadContext context) throws Exception;


    /** Context that allows a value to be read.
     *
     */
    interface ReadContext {
        String readTextValue(String key) throws Exception;
        InputStream readStreamValue(String key) throws Exception;
        default String readTextValue() throws Exception { return readTextValue((String)null);}
        default String readSingleTextValue(String key) throws Exception {return readTextValue(key);}
        default InputStream readStreamValue() throws Exception { return readStreamValue((String)null);}
        default InputStream readSingleStreamValue(String key) throws Exception { return readStreamValue(key);}
    }

    /** Context that allows a value to be written.
     *
     */
    interface WriteContext {
        void writeTextValue(String value, String key) throws Exception;
        void writeStreamValue(InputStream value, String key) throws Exception;
        default void writeTextValue(String value) throws Exception { writeTextValue(value, null);}
        default void writeSingleTextValue(String value, String key) throws Exception { writeTextValue(value, key);}
        default void writeStreamValue(InputStream value) throws Exception { writeStreamValue(value, null);}
        default void writeSingleStreamValue(InputStream value, String key) throws Exception { writeStreamValue(value, key);}
        void deleteVariable() throws Exception;
    }
}
