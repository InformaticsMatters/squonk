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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class BasicObjectJsonSerializer<T extends BasicObject> extends StdSerializer<T> {

    private static final Logger LOG = Logger.getLogger(BasicObjectJsonSerializer.class.getName());

    public BasicObjectJsonSerializer() {
        this(BasicObject.class);
    }
    
    protected BasicObjectJsonSerializer(Class cls) {
        super(cls);
    }

    @Override
    public void serialize(BasicObject bo, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonGenerationException {

        Map<String, Class> propTypes = null;
        DatasetMetadata meta = (DatasetMetadata) sp.getAttribute(JsonHandler.ATTR_DATASET_METADATA);
        if (meta != null) {
            propTypes = meta.getValueClassMappings();
        }
        if (propTypes == null) {
            propTypes = (Map<String, Class>) sp.getAttribute(JsonHandler.ATTR_VALUE_MAPPINGS);
        }
        if (propTypes == null) {
            LOG.fine("Serializing without metadata - complex types will not be handled");
            propTypes = new HashMap<>();
        }
        jg.writeStartObject();
        writeData((T)bo, jg, sp, propTypes);
        jg.writeEndObject();
    }

//    @Override
//    public void serializeWithType(BasicObject value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer)
//            throws IOException {
//
//        typeSer.writeTypePrefixForObject(value, gen);
//        //serialize(value, gen, provider); // call your customized serialize method
//        Map<String, Class> propTypes = null;
//        Metadata meta = (Metadata) provider.getAttribute(JsonHandler.ATTR_METADATA);
//        if (meta != null) {
//            propTypes = meta.getPropertyTypes();
//        }
//        if (propTypes == null) {
//            propTypes = (Map<String, Class>) provider.getAttribute(JsonHandler.ATTR_VALUE_MAPPINGS);
//        }
//        if (propTypes == null) {
//            LOG.fine("Serializing without metadata - complex types will not be handled");
//            propTypes = new HashMap<>();
//        }
//        writeData((T)value, gen, provider, propTypes);
//        typeSer.writeTypeSuffixForObject(value, gen);
//    }
//
//    @Override
//    public Class<T> handledType() {
//        return (Class<T>)BasicObject.class;
//    }

    void writeData(T bo, JsonGenerator jg, SerializerProvider sp, Map<String, Class> propTypes) throws IOException, JsonGenerationException {
        writeUUID(bo, jg, sp);
        writeValues(bo, jg, sp, propTypes);       
    }

    protected void writeUUID(T bo, JsonGenerator jg, SerializerProvider sp) throws IOException {
        if (bo.getUUID() != null) {
            jg.writeStringField("uuid", bo.getUUID().toString());
        }
    }

    protected void writeValues(T bo, JsonGenerator jg, SerializerProvider sp, Map<String, Class> propTypes) throws IOException {
        if (bo.getValues().size() > 0) {
            jg.writeFieldName("values");
            jg.writeStartObject();
            for (Map.Entry<String, Object> e : bo.getValues().entrySet()) {
                jg.writeObjectField(e.getKey(), e.getValue());
                Class current = propTypes.get(e.getKey());
                if (current == null) {
                    if (e.getValue() != null) {
                        propTypes.put(e.getKey(), e.getValue().getClass());
                    }
                } else {
                    if (e.getValue() != null && current != e.getValue().getClass()) {
                        LOG.log(Level.WARNING,
                                "Property type inconsistent. Old: {0} Found: {1}. Old value will be used, but inconsistency could cause problems.",
                                new Object[]{current.getName(), e.getValue().getClass().getName()});
                    }
                }
            }
            jg.writeEndObject();
        }
    }

}
