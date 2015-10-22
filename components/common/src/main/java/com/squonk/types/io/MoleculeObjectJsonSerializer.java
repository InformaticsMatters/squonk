package com.squonk.types.io;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.im.lac.types.MoleculeObject;
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

}
