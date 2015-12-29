package org.squonk.types.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Custom JSON deserializer for MoleculeObjects. Handles the generation of objects of the
 * appropriate class for each item in the "values" JSON object. The classes to convert to are
 * provided at runtime using a ContextAttribute named "mappings" (use the ATTR_MAPPINGS constant)
 * whose value is a Map&lt;String,Class&gt; that maps the names to the class.
 *
 * @author timbo
 */
public class MoleculeObjectJsonDeserializer extends BasicObjectJsonDeserializer<MoleculeObject> {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectJsonDeserializer.class.getName());

    private static final String PROP_UUID = "uuid";
    private static final String PROP_SOURCE = "source";
    private static final String PROP_FORMAT = "format";
    private static final String PROP_VALUES = "values";

    public MoleculeObjectJsonDeserializer() {
        super(MoleculeObject.class);
    }

    @Override
    protected MoleculeObject generateResult(Map<String, Object> fieldValues) {
        UUID uuid = (UUID) fieldValues.get(PROP_UUID);
        String source = (String) fieldValues.get(PROP_SOURCE);
        String format = (String) fieldValues.get(PROP_FORMAT);

        MoleculeObject mo = new MoleculeObject(uuid, source, format, (Map<String, Object>) fieldValues.get(PROP_VALUES));
        return mo;
    }

    @Override
    protected boolean handleField(JsonParser jp, DeserializationContext dc, String fieldName, Map<String, Object> fieldValues, Map<String, Class> mappings) throws IOException {

        switch (fieldName) {
            case PROP_SOURCE:
                jp.nextToken();
                String source = jp.readValueAs(String.class);
                fieldValues.put(PROP_SOURCE, source);
                return true;
            case PROP_FORMAT:
                jp.nextToken();
                String format = jp.readValueAs(String.class);
                fieldValues.put(PROP_FORMAT, format);
                return true;
        }

        return super.handleField(jp, dc, fieldName, fieldValues, mappings);

    }

}
