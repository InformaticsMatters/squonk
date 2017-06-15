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
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.squonk.util.Colors;

import java.awt.*;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Custom JSON deserializer for Color. Saves as rgba hex value.
 *
 * @author Tim Dudgeon
 */
public class ColorJsonDeserializer extends StdDeserializer<Color> {

    private static final Logger LOG = Logger.getLogger(ColorJsonDeserializer.class.getName());

    public ColorJsonDeserializer() {
        super(Color.class);
    }


    @Override
    public Color deserialize(JsonParser jp, DeserializationContext dc) throws IOException {

        LOG.fine("Reading JSON");
        JsonToken currentToken = jp.getCurrentToken();
        if (currentToken == JsonToken.START_OBJECT) {
            jp.nextToken(); // field name
            String name = jp.getCurrentName();
            if (!"argb".equals(name)) {
                throw new IOException("Invalid field name. Expect rgba, found " + name);
            }
            jp.nextToken();
            String hex = jp.readValueAs(String.class);
            jp.nextToken(); // end object
            return Colors.rgbaHexToColor(hex);
        } else {
            throw new IOException("Invalid JSON for Color");
        }

    }

}
