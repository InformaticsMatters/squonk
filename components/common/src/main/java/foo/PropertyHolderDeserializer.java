package foo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class PropertyHolderDeserializer extends StdDeserializer<PropertyHolder> {

    private final Map<String, Class> mappings;

    public PropertyHolderDeserializer(Map<String, Class> mappings) {
        super(PropertyHolder.class);
        this.mappings = mappings;
    }

    @Override
    public PropertyHolder deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        System.out.println("PropertyHolderDeserializer.deserialize()");
        JsonToken current = jp.getCurrentToken();
        System.out.println("Current token: " + current);
        //current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected start object, found " + current);
        }

        PropertyHolder ph = new PropertyHolder();
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            Class cls = mappings.get(fieldName);
            jp.nextToken();
            if (cls != null) {
                Object o = jp.readValueAs(cls);
                System.out.println("Read object of class " + cls.getName() + " -> " + o);
                ph.putProperty(fieldName, o);
            }
        }

        return ph;
    }

}
