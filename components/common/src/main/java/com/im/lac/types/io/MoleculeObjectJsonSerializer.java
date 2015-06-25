package com.im.lac.types.io;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class MoleculeObjectJsonSerializer extends StdSerializer<MoleculeObject> {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectJsonSerializer.class.getName());

    public MoleculeObjectJsonSerializer() {
        super(MoleculeObject.class);
    }

    @Override
    public void serialize(MoleculeObject mo, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonGenerationException {
        Metadata meta = (Metadata) sp.getAttribute(JsonHandler.ATTR_METADATA);
        Map<String, Class> propTypes = meta.getPropertyTypes();
        //sp.defaultSerializeValue(mo, jg);
        jg.writeStartObject();

        if (mo.getUUID() != null) {
            jg.writeStringField("uuid", mo.getUUID().toString());
        }
        if (mo.getSource() != null) {
            jg.writeStringField("source", mo.getSource());
        }
        if (mo.getFormat() != null) {
            jg.writeStringField("format", mo.getFormat());
        }
        if (mo.getValues().size() > 0) {
            jg.writeFieldName("values");
            jg.writeStartObject();
            for (Map.Entry<String, Object> e : mo.getValues().entrySet()) {
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
        jg.writeEndObject();

    }

}
