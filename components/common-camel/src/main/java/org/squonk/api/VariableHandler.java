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

import org.squonk.io.IODescriptor;
import org.squonk.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    /** Create the variable handled by this handler
     *
     * @param input An InputStream from which the value must be composed
     * @return The assembled value
     */
    T create(InputStream input) throws Exception;


    /** Create an instance from one or more InputStreams. The inputs are passed in as a Map with the key identifying the
     * type of input. Where there is only a single input the name is ignored and the
     * {@link #create(InputStream)} method is called with that one input.
     * Where there are multiple inputs the {@link #createMultiple(Map<String,InputStream>)} method is
     * called that MUST be overrided by any subclass wanting to handle multiple inputs. The overriding method should use
     * the names that are present as the keys to the Map to distinguish the different inputs.
     *
     * @param inputs multiple InputStreams where the key is a name that identifies the type of input
     * @return
     * @throws Exception
     */
    T create(Map<String,InputStream> inputs) throws Exception;

    T createMultiple(Map<String,InputStream> inputs) throws Exception;


    /** Context that allows a value to be read.
     *
     */
    interface ReadContext {

        String readTextValue(String mediaType, String extension, String key) throws Exception;

        default String readTextValue(String mediaType, String extension) throws Exception {
            return readTextValue(mediaType, extension, null);
        }

        InputStream readStreamValue(String mediaType, String extension, String key) throws Exception;

        default InputStream readStreamValue(String mediaType, String extension) throws Exception {
            return readStreamValue(mediaType, extension, null);
        }
    }

    /** Context that allows a value to be written.
     *
     */
    interface WriteContext {
        void writeTextValue(String value, String mediaType, String extension, String key) throws Exception;
        void writeStreamValue(InputStream value, String mediaType, String extension, String key, boolean gzip) throws Exception;
        default void writeTextValue(String value, String mediaType, String extension) throws Exception {
            writeTextValue(value, mediaType, extension, null);
        }
        void deleteVariable() throws Exception;
    }
}
