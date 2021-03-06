/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

import org.squonk.io.SquonkDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
public interface VariableHandler<T> extends Handler<T> {


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

    List<SquonkDataSource> readDataSources(ReadContext context) throws Exception;

    /** Create the variable handled by this handler
     *
     * @param input An SquonkDataSource from which the value must be composed
     * @return The assembled value
     */
    T create(SquonkDataSource input) throws Exception;


    /** Create an instance from one or more SquonkDataSources. The name of the dataSource identifies the
     * type of input. Where there is only a single input the name is ignored and the
     * {@link #create(SquonkDataSource)} method is called with that one input.
     *
     * @param inputs multiple datasources defining the variable.
     * @return
     * @throws Exception
     */
    T create(List<SquonkDataSource> inputs) throws Exception;

    T create(String mediaType, Class genericType, Map<String, InputStream> inputs) throws Exception;

    /** Context that allows a value to be read.
     *
     */
    interface ReadContext {

        String readTextValue(String mediaType, String role, String key) throws Exception;

        default String readTextValue(String mediaType, String role) throws Exception {
            return readTextValue(mediaType, role, null);
        }

        SquonkDataSource readStreamValue(String mediaType, String role, String key) throws Exception;

        default SquonkDataSource readStreamValue(String mediaType, String role) throws Exception {
            return readStreamValue(mediaType, role, null);
        }

    }

    /** Context that allows a value to be written.
     *
     */
    interface WriteContext {
        void writeTextValue(String value, String mediaType, String role, String key) throws Exception;
        void writeStreamValue(InputStream value, String mediaType, String role, String key, boolean gzip) throws Exception;
        default void writeTextValue(String value, String mediaType, String role) throws Exception {
            writeTextValue(value, mediaType, role, null);
        }
        void deleteVariable() throws Exception;
    }
}
