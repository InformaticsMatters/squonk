package org.squonk.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.squonk.util.ServiceConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author timbo
 */
public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public static <T> T fromJson(InputStream is, Class<T> type) throws IOException {
        return mapper.readValue(is, type);
    }

    public static <T> T fromJson(String s, Class<T> type) throws IOException {
        return mapper.readValue(s, type);
    }

    public static String fetchUsername(Exchange exchange) {
        String username = exchange.getIn().getHeader(ServiceConstants.HEADER_SQUONK_USERNAME, String.class);
        if (username == null) {
            throw new IllegalStateException("Validated username not specified");
        }
        return username;
    }

}
