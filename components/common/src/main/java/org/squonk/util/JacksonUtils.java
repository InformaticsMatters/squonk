package org.squonk.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Jackson utility methods for handling JSON
 *
 * @author timbo
 */
public class JacksonUtils {

    private static Logger LOG = Logger.getLogger(JacksonUtils.class.getName());

    /**
     * Take a JSON object and build a map of values whose types are specified by
     * the mappings. Jackson must know how to deserialize (e.g. using
     * annotations on the classes or by registering custom deserializers).
     *
     * @param jp
     * @param mappings
     * @param values
     * @throws IOException
     */
    public static void buildTypedMap(JsonParser jp, Map<String, Class> mappings, Map<String, Object> values)
            throws IOException {
        if (mappings == null) {
            LOG.warning("Field to class mappings not found. Using default JSON types only.");
            mappings = Collections.emptyMap();
        }
        JsonToken current = jp.getCurrentToken();
        if (current != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected start object, found " + current);
        }

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            Class cls = mappings.get(fieldName);
            current = jp.nextToken();
            if (cls != null) {
                Object o = jp.readValueAs(cls);
                LOG.log(Level.FINE, "Read object of class {0} -> {1}", new Object[]{cls.getName(), o});
                values.put(fieldName, o);
            } else {
                values.put(fieldName, defaultRead(jp));
            }
        }
    }

    /**
     * Default read value when no mapping is provided. Hopefully never happens.
     * Might be replaceable with JsonParser.getCurrentValue() once Jackson 2.5
     * is used.
     *
     * @param jp The JSON parser
     * @return The value
     * @throws IOException
     */
    public static Object defaultRead(JsonParser jp) throws IOException {

        switch (jp.getCurrentToken()) {
            case VALUE_STRING:
                return jp.getText();
            case VALUE_NUMBER_INT:
                return jp.getIntValue();
            case VALUE_NUMBER_FLOAT:
                return jp.getFloatValue();
            case VALUE_FALSE:
            case VALUE_TRUE:
                return jp.getBooleanValue();
            case VALUE_NULL:
                return null;
            case START_ARRAY:
                JsonToken tok = jp.nextToken();
                List values = new ArrayList();
                while (tok != JsonToken.END_ARRAY) {
                    Object val = defaultRead(jp);
                    tok = jp.nextToken();
                    values.add(val);
                }
                return values;
            default:
                LOG.log(Level.INFO, "Unexpected JSON token: {0} - handling as String", jp.getCurrentToken());
                return jp.getText();
        }
    }
}
