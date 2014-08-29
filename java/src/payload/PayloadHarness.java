package payload;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author simetrias
 */
public class PayloadHarness {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        TypeReference<List<Payload>> payloadListType = new TypeReference<List<Payload>>() {
        };
        List<Payload> payloadList = new ArrayList<Payload>();

        Payload payload;
        HashMap<String, String> properties;

        payload = new Payload();
        payload.setId(1l);
        payload.setDescription("First item");
        properties = new HashMap<String, String>();
        payload.setProperties(properties);
        properties.put("Gustavo", "Santucho");
        properties.put("Gabriel", "Moreno");
        payloadList.add(payload);

        payload = new Payload();
        payload.setId(2l);
        payload.setDescription("Second item");
        properties = new HashMap<String, String>();
        payload.setProperties(properties);
        properties.put("Santiago", "Santucho");
        properties.put("Mario", "Burdman");
        payloadList.add(payload);

        // test serialization to JSON
        String json = objectMapper.writeValueAsString(payloadList);
        System.out.println(json);

        // test deserialization from JSON
        List<Payload> receivedPayloadList = objectMapper.readValue(json, payloadListType);
        Payload receivedPayload = receivedPayloadList.get(0);
        System.out.println(receivedPayload.getDescription() + " - " + receivedPayload.getProperties().get("Gustavo"));
    }

}
