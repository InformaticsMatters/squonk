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

package org.squonk.execution.variable.impl;

import org.squonk.api.VariableHandler;
import org.squonk.client.VariableClient;
import org.squonk.core.client.NotebookRestClient;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 13/03/16.
 */
public class VariableWriteContext implements VariableHandler.WriteContext {

    private static final Logger LOG = Logger.getLogger(VariableWriteContext.class.getName());

    private final VariableClient client;
    private final Long notebookId;
    private final Long editableId;
    private final Long cellId;
    private final String variableName;

    public VariableWriteContext(VariableClient client, Long notebookId, Long editableId, Long cellId, String variableName) {
        this.client = client;
        this.notebookId = notebookId;
        this.editableId = editableId;
        this.cellId = cellId;
        this.variableName = variableName;
    }

    @Override
    public void writeTextValue(String value, String mediaType, String extension, String key) throws Exception {
        client.writeTextValue(notebookId, editableId, cellId, variableName, value, key);
    }

    @Override
    public void writeStreamValue(InputStream value, String mediaType, String extension, String key, boolean gzip) throws Exception {
        client.writeStreamValue(notebookId, editableId, cellId, variableName, gzip ? IOUtils.getGzippedInputStream(value) : value, key);
    }

    @Override
    public void deleteVariable() throws Exception {

    }


}
