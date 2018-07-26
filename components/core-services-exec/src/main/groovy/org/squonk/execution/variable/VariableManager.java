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

import org.squonk.api.VariableHandler;
import org.squonk.client.VariableClient;
import org.squonk.execution.variable.impl.VariableReadContext;
import org.squonk.execution.variable.impl.VariableWriteContext;
import org.squonk.io.InputStreamDataSource;
import org.squonk.io.SquonkDataSource;
import org.squonk.notebook.api.VariableKey;
import org.squonk.types.AbstractStreamType;
import org.squonk.types.TypeResolver;
import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manager for variables that uses the VariableClient to persist values, and also allows temporary storage of variables.
 * Temp variables are distinguished by starting with an underscore character (_).
 *
 * If the VariableClient is null then all variables are stored as temp variables (this can be useful for testing)
 *
 * @author timbo
 */
public class VariableManager {

    public static Logger LOG = Logger.getLogger(VariableManager.class.getName());

    private static final TypeResolver typeResolver = TypeResolver.getInstance();
    private final Map<String, byte[]> tmpValues = new ConcurrentHashMap<>();

    private final VariableClient client;
    private final Long notebookId;
    private final Long sourceId;

    public VariableManager(VariableClient client, Long notebookId, Long sourceId) {
        this.client = client;
        this.notebookId = notebookId;
        this.sourceId = sourceId;
    }

    public String getTmpVariableInfo() {
        StringBuilder b = new StringBuilder("Temp variables:\n");
        tmpValues.forEach((k,v) -> b.append("  ").append(k).append(" size ").append(v.length).append("\n"));
        return b.toString();
    }

    public <V> void putValue(VariableKey key, Class<V> type, V value) throws Exception {
        putValue(key, type, null, value);
    }

    /** Write the value as a variable e.g. using the VariableHandler.WriteContext for this variableManager
     *
     * @param key
     * @param primaryType
     * @param secondaryType
     * @param value
     * @param <V>
     * @throws Exception
     */
    public <V> void putValue(VariableKey key, Class<V> primaryType, Class secondaryType, V value) throws Exception {
        LOG.fine("putValue: " + key + " -> " + value);
        VariableHandler.WriteContext context = createWriteContext(key);
        putValue(primaryType, secondaryType, value, context);
    }

    public <V> void putValue(Class<V> type, V value, VariableHandler.WriteContext context) throws Exception {
        putValue(type, null, value, context);
    }

    /** Write the value using the specified context. Useful for writing to files etc.
     *
     * @param primaryType
     * @param secondaryType
     * @param value
     * @param <V>
     * @param context The context to write the data to
     * @throws Exception
     */
    public <V> void putValue(Class<V> primaryType, Class secondaryType, V value, VariableHandler.WriteContext context) throws Exception {

        VariableHandler<V> vh = typeResolver.createVariableHandler(primaryType, secondaryType);

        if (vh != null) {
            LOG.fine("Using write variable handler " + vh + " for type " + primaryType.getName());
            vh.writeVariable(value, context);
        } else {
            String mediaType = TypeResolver.getInstance().resolveMediaType(primaryType, secondaryType);
            if (value instanceof AbstractStreamType) {
                LOG.info("No variable handler for type " + primaryType.getName() + ". Handling as stream");
                context.writeStreamValue(((AbstractStreamType)value).getInputStream(), mediaType, null, null, true);
            } else {
                LOG.info("No variable handler for type " + primaryType.getName() + ". Handling as text");
                context.writeTextValue(value.toString(), mediaType, null);
            }
        }
    }

    public <V> V getValue(VariableKey key, Class<V> primaryType) throws Exception {
        return getValue(key, primaryType, null);
    }

    /** Read the value as a variable e.g. using the VariableHandler.ReadContext for this variableManager
     *
     * @param key
     * @param primaryType
     * @param secondaryType
     * @param <V>
     * @return
     * @throws Exception
     */
    public <V> V getValue(VariableKey key, Class<V> primaryType, Class secondaryType) throws Exception {
        LOG.fine("getValue " + key + " of type " + primaryType);
        VariableHandler.ReadContext context = createReadContext(key);
        return getValue(primaryType, secondaryType, context);
    }

    /**  Read the value using the specified context. Useful for reading from files etc.
     *
     * @param primaryType
     * @param secondaryType
     * @param context
     * @param <V>
     * @return
     * @throws Exception
     */
    public <V> V getValue(Class<V> primaryType, Class secondaryType, VariableHandler.ReadContext context) throws Exception {

        VariableHandler<V> vh = typeResolver.createVariableHandler(primaryType, secondaryType);
        LOG.info("Using read variable handler " + vh + " for type " + primaryType.getName());

        if (vh != null) {
            V result = (V) vh.readVariable(context);
            return result;

        } else {
            String mediaType = TypeResolver.getInstance().resolveMediaType(primaryType, null);
            if (canBeHandledAs(primaryType, SquonkDataSource.class)) {
                Constructor c = primaryType.getConstructor(SquonkDataSource.class);
                SquonkDataSource ds = context.readStreamValue(mediaType, null);
                return ds == null ? null : (V) c.newInstance(ds);
            } else if (canBeHandledAs(primaryType, InputStream .class)) {
                Constructor c = primaryType.getConstructor(InputStream.class);
                SquonkDataSource ds = context.readStreamValue(mediaType, null);
                return ds == null ? null : (V) c.newInstance(ds.getInputStream());
            } else if (canBeHandledAs(primaryType, String .class)) {
                Constructor c = primaryType.getConstructor(String.class);
                String s = context.readTextValue(mediaType, null);
                return s == null ? null : (V) c.newInstance(s);
            }
        }
        throw new IllegalArgumentException("Don't know how to handle value of type " + primaryType.getName());
    }

    public <S,T> T convertValue(Class<S> from, Class<T> to, S value) {
        return null;
    }


    /** Can the testCls be handled as type targetClass
     *
     * @param testCls
     * @param targetCls
     * @return
     */
    boolean canBeHandledAs(Class testCls, Class targetCls) {
        for (Constructor c : testCls.getConstructors()) {
            if (c.getParameterCount() == 1 && c.getParameterTypes()[0].isAssignableFrom(targetCls)) {
                return true;
            }
        }
        return false;
    }

    private VariableHandler.WriteContext createWriteContext(VariableKey key) {
        if (client == null || key.getVariableName().startsWith("_")) {
            return new TmpContext(key.getCellId(), key.getVariableName());
        } else {
            return new VariableWriteContext(client, notebookId, sourceId, key.getCellId(), key.getVariableName());
        }
    }

    private VariableHandler.ReadContext createReadContext(VariableKey key) {
        if (client == null || key.getVariableName().startsWith("_")) {
            return new TmpContext(key.getCellId(), key.getVariableName());
        } else {
            return new VariableReadContext(client, notebookId, sourceId, key.getCellId(), key.getVariableName());
        }
    }


    class TmpContext implements VariableHandler.WriteContext, VariableHandler.ReadContext {

        Long cellId;
        String variableName;

        TmpContext(Long cellId, String variableName) {
            this.cellId = cellId;
            this.variableName = variableName;
        }

        String generateTextKey(String key) {
            return "T#" + notebookId + "#" + sourceId + "#" + cellId + "#" + variableName + "#" + key;
        }

        String generateStreamKey(String key) {
            return "S#" + notebookId + "#" + sourceId + "#" + cellId + "#" + variableName + "#" + key;
        }

        @Override
        public String readTextValue(String mediaType, String extension, String key) throws Exception {
            String storeKey = generateTextKey(key);
            LOG.fine("Reading tmp value " + storeKey);
            byte[] bytes = tmpValues.get(storeKey);
            return bytes == null ? null : new String(bytes);
        }

        @Override
        public SquonkDataSource readStreamValue(String mediaType, String extension, String key) throws Exception {
            String storeKey = generateStreamKey(key);
            LOG.fine("Reading tmp value " + storeKey);
            byte[] bytes = tmpValues.get(storeKey);
            if (bytes == null) {
                return null;
            }
            InputStream is = new ByteArrayInputStream(bytes);
            return new InputStreamDataSource(variableName, mediaType, is, null);
        }

        @Override
        public void writeTextValue(String value, String mediaType, String extension, String key) throws Exception {
            String storeKey = generateTextKey(key);
            LOG.fine("Writing tmp value " + storeKey);
            if (value == null) {
                tmpValues.remove(storeKey);
            } else {
                tmpValues.put(storeKey, value.getBytes());
            }
        }

        @Override
        public void writeStreamValue(InputStream value, String mediaType, String extension, String key, boolean gzip) throws Exception {
            String storeKey = generateStreamKey(key);
            LOG.fine("Writing tmp value " + storeKey);
            if (value == null) {
                tmpValues.remove(storeKey);
            } else {
                tmpValues.put(storeKey, IOUtils.convertStreamToBytes(gzip ? IOUtils.getGzippedInputStream(value) : value));
            }
        }

        @Override
        public void deleteVariable() throws Exception {
            tmpValues.entrySet().removeIf(e -> e.getKey().startsWith("T#" + notebookId + "#" + sourceId + "#" + cellId + "#" + variableName + "#")
            || e.getKey().startsWith("S#" + notebookId + "#" + sourceId + "#" + cellId + "#" + variableName + "#"));
        }
    }

}
