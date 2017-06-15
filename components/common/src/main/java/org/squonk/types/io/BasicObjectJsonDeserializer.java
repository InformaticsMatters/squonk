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

package org.squonk.types.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.util.JacksonUtils;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom JSON deserializer for BasicObjects. Handles the generation of objects of the appropriate
 * class for each item in the "values" JSON object. The classes to convert to are provided at
 * runtime using a ContextAttribute named "mappings" (use the ATTR_MAPPINGS constant) whose value is
 * a Map&lt;String,Class&gt; that maps the names to the class.
 *
 * @author Tim Dudgeon
 */
public class BasicObjectJsonDeserializer<T extends BasicObject> extends StdDeserializer<T> {

    private static final Logger LOG = Logger.getLogger(BasicObjectJsonDeserializer.class.getName());

    private static final String PROP_UUID = "uuid";
    private static final String PROP_VALUES = "values";

    public BasicObjectJsonDeserializer() {
        super(BasicObject.class);
    }

    public BasicObjectJsonDeserializer(Class<T> cls) {
        super(cls);
    }

    @Override
    public T deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {

        LOG.fine("Reading JSON");
        JsonToken currentToken = jp.getCurrentToken();
        T result = null;
        switch (currentToken) {
            case START_ARRAY:
                jp.nextToken();
                result = deserialize(jp, dc);
                break;
            case END_ARRAY:
                jp.nextToken();
                break;
            case START_OBJECT:
                result = read(jp, dc);
                break;
            case END_OBJECT:
                jp.nextToken();
                break;
        }
        return result;
    }

//    @Override
//    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
//        return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
//    }
//
//    @Override
//    public Class<?> handledType() {
//        return BasicObject.class;
//    }

    private T read(JsonParser jp, DeserializationContext dc) throws IOException {

        Map<String, Object> fieldValues = new HashMap<>();
        JsonToken currentToken;
        int depth = 0;

        Map<String, Class> mappings = null;
        DatasetMetadata metadata = (DatasetMetadata) dc.getAttribute(JsonHandler.ATTR_DATASET_METADATA);
        if (metadata != null) {
            mappings = metadata.getValueClassMappings();
        }
        if (mappings == null) {
            mappings = (Map<String, Class>) dc.getAttribute(JsonHandler.ATTR_VALUE_MAPPINGS);
        }
        if (mappings == null) {
            LOG.fine("Deserializing without metadata - complex types will not be handled");
            mappings = Collections.emptyMap();
        }

        while (true) {
            currentToken = jp.nextToken();
            if (currentToken == null) {
                break;
            }

            if (currentToken == JsonToken.END_OBJECT) {
                if (depth == 0) {
                    break;
                } else {
                    depth--;
                }
            }
            switch (currentToken) {
                case START_OBJECT:
                    depth++;
                    break;
                case FIELD_NAME:
                    String name = jp.getCurrentName();
                    handleField(jp, dc, name, fieldValues, mappings);
                    break;
                default:
                    LOG.log(Level.WARNING, "Unexpected token type: {0}", currentToken);
            }

        }
        return generateResult(fieldValues);
    }

    protected T generateResult(Map<String, Object> fieldValues) {
        UUID uuid = (UUID) fieldValues.get(PROP_UUID);
        BasicObject bo = new BasicObject(uuid, (Map<String, Object>) fieldValues.get(PROP_VALUES));
        return (T) bo;
    }

    protected boolean handleField(JsonParser jp, DeserializationContext dc, String fieldName, Map<String, Object> fieldValues, Map<String, Class> mappings) throws IOException {

        switch (fieldName) {
            case PROP_UUID:
                jp.nextToken();
                UUID uuid = jp.readValueAs(UUID.class);
                fieldValues.put(PROP_UUID, uuid);
                return true;
            case PROP_VALUES:
                jp.nextToken();
                Map<String, Object> values = new HashMap<>();
                JacksonUtils.buildTypedMap(jp, mappings, values);
                fieldValues.put(PROP_VALUES, values);
                return true;
            default:
                LOG.log(Level.WARNING, "Unexpected field name: {0}", fieldName);
        }
        return false;
    }
}
