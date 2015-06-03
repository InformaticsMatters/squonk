package com.im.lac.types.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.JacksonUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom JSON deserializer for MoleculeObjects.
 * Handles the generation of objects of the appropriate class for each item in the "values"
 * JSON object. The classes to convert to are provided at runtime using a ContextAttribute 
 * named "mappings" (use the ATTR_MAPPINGS constant) whose value is a Map&lt;String,Class&gt; 
 * that maps the names to the class.
 *
 * @author timbo
 */
public class MoleculeObjectJsonDeserializer extends StdDeserializer<MoleculeObject> {
    
    public static final String ATTR_MAPPINGS = "mappings";
    
    private static final Logger LOG = Logger.getLogger(MoleculeObjectJsonDeserializer.class.getName());

    private static final String PROP_SOURCE = "source";
    private static final String PROP_FORMAT = "format";
    private static final String PROP_VALUES = "values";

    public MoleculeObjectJsonDeserializer() {
        super(MoleculeObject.class);
    }

    @Override
    public MoleculeObject deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {

        LOG.fine("Reading JSON");
        JsonToken currentToken = jp.getCurrentToken();
        MoleculeObject result = null;
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

    private MoleculeObject read(JsonParser jp, DeserializationContext dc) throws IOException {

        String source = null;
        String format = null;
        Map<String, Object> values = new HashMap<>();
        JsonToken currentToken;
        int depth = 0;
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
                    switch (name) {
                        case PROP_SOURCE:
                            jp.nextToken();
                            source = jp.readValueAs(String.class);
                            break;
                        case PROP_FORMAT:
                            jp.nextToken();
                            format = jp.readValueAs(String.class);
                            //System.out.println("format is: " + format);
                            break;
                        case PROP_VALUES:
                            jp.nextToken();
                            //readValues(jp, dc, values);
                            Map<String, Class> mappings = (Map<String, Class>) dc.getAttribute(ATTR_MAPPINGS);
                            JacksonUtils.buildTypedMap(jp, mappings, values);
                            break;
                        default:
                            LOG.log(Level.WARNING, "Unexpected field name: {0}", name);
                    }
                    break;
                default:
                    LOG.log(Level.WARNING, "Unexpected token type: {0}", currentToken);
            }

        }

        MoleculeObject mo = new MoleculeObject(source, format);
        if (values.size() > 0) {
            mo.putValues(values);
        }
        return mo;
    }

}
