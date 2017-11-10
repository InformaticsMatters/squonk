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

package org.squonk.client;

import java.io.InputStream;

/**
 * Interface for persisting notebook variables.
 *
 * Created by timbo on 08/03/16.
 */
public interface VariableClient {

    String DEFAULT_KEY = "default";

    enum VarType { t, s }

    /**
     *
     * @param notebookId
     * @param sourceId
     * @param variableName
     * @return
     * @throws Exception
     */
    default String readTextValue(Long notebookId, Long sourceId, Long cellId, String variableName) throws Exception {
        return readTextValue(notebookId, sourceId, cellId, variableName, DEFAULT_KEY);
    }

    /**
     *
     * @param notebookId
     * @param sourceId Can be a editable ID or a savepoint ID
     * @param variableName
     * @param key
     * @return
     * @throws Exception
     */
    String readTextValue(Long notebookId, Long sourceId, Long cellId, String variableName, String key) throws Exception;

//    /** Read the variable with the default key for the notebook version with the specified label.
//     *
//     * @param notebookId
//     * @param label
//     * @param variableName
//     * @return
//     */
//    default String readTextValue(Long notebookId, String label, String variableName) throws Exception {
//        return readTextValue(notebookId, label, variableName, DEFAULT_KEY);
//    }

//    /** Read the variable with the specified key for the notebook version with the specified label.
//     *
//     * @param notebookId
//     * @param label
//     * @param variableName
//     * @param key
//     * @return
//     * @throws Exception
//     */
//    String readTextValue(Long notebookId, String label, String variableName, String key) throws Exception;

    /** Save this variable using the default key name of 'default'.
     * Use this for single component variables.
     *
     * @param notebookId
     * @param editableId
     * @param cellId
     * @param variableName
     * @param value
     * @throws Exception
     */
    default void writeTextValue(Long notebookId, Long editableId, Long cellId, String variableName, String value) throws Exception {
        writeTextValue(notebookId, editableId, cellId, variableName, value, DEFAULT_KEY);
    }

    /** Save this variable using the specified key.
     * Use this for multi component variables where each part is saved under a different key name, but with the same variable name.
     * If the combination of editableId, variableName and key already exists this is an update operation, if not then it
     * inserts a new row.
     *
     * @param notebookId
     * @param editableId
     * @param cellId
     * @param variableName
     * @param value
     * @param key
     * @throws Exception
     */
    void writeTextValue(Long notebookId, Long editableId, Long cellId, String variableName, String value, String key) throws Exception;

    /**
     * @param notebookId
     * @param sourceId
     * @param variableName
     * @return An InputStream to the data. Ensure that this is closed when finished
     * @throws Exception
     */
    default InputStream readStreamValue(Long notebookId, Long sourceId, Long cellId, String variableName) throws Exception {
        return readStreamValue(notebookId, sourceId, cellId, variableName, DEFAULT_KEY);
    }

    /** Read a stream variable
     *
     * @param notebookId
     * @param sourceId Can be a editable ID or a savepoint ID
     * @param variableName
     * @param key
     * @return An InputStream to the data. Ensure that this is closed when finished
     * @throws Exception
     */
    InputStream readStreamValue(Long notebookId, Long sourceId, Long cellId, String variableName, String key) throws Exception;

//    /**
//     *
//     * @param notebookId
//     * @param label
//     * @param variableName
//     * @return An InputStream to the data. Ensure that this is closed when finished
//     * @throws Exception
//     */
//    default InputStream readStreamValue(Long notebookId, String label, String variableName) throws Exception {
//        return readStreamValue(notebookId, label, variableName, DEFAULT_KEY);
//    }

//    /**
//     * @param notebookId
//     * @param label
//     * @param variableName
//     * @param key
//     * @return An InputStream to the data. Ensure that this is closed when finished
//     * @throws Exception
//     */
//    InputStream readStreamValue(Long notebookId, String label, String variableName, String key) throws Exception;

    /**
     * @param notebookId
     * @param editableId
     * @param cellId
     * @param variableName
     * @param value
     * @throws Exception
     */
    default void writeStreamValue(Long notebookId, Long editableId, Long cellId, String variableName, InputStream value) throws Exception {
        writeStreamValue(notebookId, editableId, cellId, variableName, value, DEFAULT_KEY);
    }


    /**
     * @param
     * @param editableId
     * @param cellId
     * @param variableName
     * @param key
     * @param value
     * @throws Exception
     */
    void writeStreamValue(Long notebookId, Long editableId, Long cellId, String variableName, InputStream value, String key) throws Exception;

    void deleteVariable(Long notebookId, Long editableId, Long cellId, String variableName) throws Exception;

}
