package foo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;

/**
 *
 * @author timbo
 */
public class MoleculeObjectDeserializer extends StdDeserializer<MoleculeObject> {

    private static final String PROP_SOURCE = "source";
    private static final String PROP_FORMAT = "format";
    private static final String PROP_VALUES = "values";

    public MoleculeObjectDeserializer() {
        super(MoleculeObject.class);
    }

    @Override
    public MoleculeObject deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        System.out.println("Reading JSON");
        JsonToken currentToken = jp.getCurrentToken();
        System.out.println("Token: " + currentToken);
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

//    private MoleculeObject read(JsonParser jp, DeserializationContext dc) throws IOException {
//
//        String source = null;
//        String format = null;
//        PropertyHolder ph = null;
//
//        JsonToken currentToken = jp.getCurrentToken();
//        System.out.println("  " + currentToken);
//        int depth = 0;
//        while (true) {
//            currentToken = jp.nextToken();
//            if (currentToken == null) {
//                break;
//            }
//            System.out.println("  " + currentToken);
//
//            if (currentToken == JsonToken.END_OBJECT) {
//                if (depth == 0) {
//                    break;
//                } else {
//                    depth--;
//                }
//            }
//            switch (currentToken) {
//                case START_OBJECT:
//                    depth++;
//                    break;
//                case FIELD_NAME:
//                    String name = jp.getCurrentName();
//                    switch (name) {
//                        case PROP_SOURCE:
//                            jp.nextToken();
//                            source = jp.readValueAs(String.class);
//                            System.out.println("source is: " + source);
//                            break;
//                        case PROP_FORMAT:
//                            jp.nextToken();
//                            format = jp.readValueAs(String.class);
//                            System.out.println("format is: " + format);
//                            break;
//                        case PROP_VALUES:
//                            jp.nextToken();
//                            ph = jp.readValueAs(PropertyHolder.class);
//                            break;
//                        default:
//                            System.out.println("Unexpected name: " + name);
//                    }
//                    break;
//                default:
//                    System.out.println("Unexpected type: " + currentToken);
//            }
//
//        }
//
//        MoleculeObject mo = new MoleculeObject(source, format);
//        if (ph != null) {
//            mo.putValues(ph.getValues());
//        }
//        return mo;
//    }

    private MoleculeObject read(JsonParser jp, DeserializationContext dc) throws IOException {
        JsonNode tree = jp.readValueAsTree();
        System.out.println("Read node " + tree);

        String sourceV = null;
        String formatV = null;
        JsonNode sourceN = tree.get(PROP_SOURCE);
        JsonNode formatN = tree.get(PROP_FORMAT);
        JsonNode valuesN = tree.get(PROP_VALUES);
        if (sourceN != null) {
            sourceV = sourceN.asText();
        }
        if (formatN != null) {
            formatV = formatN.asText();
        }
        MoleculeObject mo = new MoleculeObject(sourceV, formatV);

        if (valuesN != null) {
            PropertyHolder ph = valuesN.traverse(jp.getCodec()).readValueAs(PropertyHolder.class);
            mo.putValues(ph.getValues());
        }
        return mo;
    }
}
