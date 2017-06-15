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
import org.squonk.types.MoleculeObject;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Tim Dudgeon
 */
public class MoleculeObjectJsonSerializer extends BasicObjectJsonSerializer<MoleculeObject> {

    //private static final Logger LOG = Logger.getLogger(MoleculeObjectJsonSerializer.class.getName());

    public MoleculeObjectJsonSerializer() {
        super(MoleculeObject.class);
    }

    @Override
    void writeData(MoleculeObject mo, JsonGenerator jg, SerializerProvider sp, Map<String, Class> propTypes) throws IOException, JsonGenerationException {
        writeUUID(mo, jg, sp);
        writeMoleculeData(mo, jg, sp);
        writeValues(mo, jg, sp, propTypes);
    }

    void writeMoleculeData(MoleculeObject mo, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonGenerationException {

        if (mo.getSource() != null) {
            jg.writeStringField("source", mo.getSource());
        }
        if (mo.getFormat() != null) {
            jg.writeStringField("format", mo.getFormat());
        }
    }

    @Override
    public Class<MoleculeObject> handledType() {
        return MoleculeObject.class;
    }

}
