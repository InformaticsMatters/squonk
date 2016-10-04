package org.squonk.types.io;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.squonk.util.Colors;

import java.awt.Color;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class ColorJsonSerializer extends StdSerializer<Color> {

    private static final Logger LOG = Logger.getLogger(ColorJsonSerializer.class.getName());

    public ColorJsonSerializer() {
        super(Color.class);
    }

    @Override
    public void serialize(Color c, JsonGenerator jg, SerializerProvider sp) throws IOException {

        jg.writeStartObject();
        jg.writeStringField("argb", Colors.rgbaColorToHex(c));
        jg.writeEndObject();

    }

}
