package org.squonk.camel.dataformat;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.squonk.types.io.JsonHandler;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 *
 * @author timbo
 */
public class SimpleJsonDataFormat implements DataFormat {
    
    private final ObjectReader reader;
    private final ObjectWriter writer;
    
    public SimpleJsonDataFormat(Class type) {
        reader = JsonHandler.getInstance().getObjectMapper().readerFor(type);
        writer = JsonHandler.getInstance().getObjectMapper().writerFor(type);
    }

    @Override
    public void marshal(Exchange exchng, Object o, OutputStream out) throws Exception {
        writer.writeValue(out, o);
    }

    @Override
    public Object unmarshal(Exchange exchng, InputStream in) throws Exception {
        return reader.readValue(in);
    }
    
}
