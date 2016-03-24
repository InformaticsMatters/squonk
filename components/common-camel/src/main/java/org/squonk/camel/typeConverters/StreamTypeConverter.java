package org.squonk.camel.typeConverters;

import com.im.lac.util.StreamProvider;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @author timbo
 */
@Converter
public class StreamTypeConverter {

    @Converter
    public static Iterator convertStreamToIterator(Stream s, Exchange exchange) {
        return s.iterator();
    }

    @Converter
    public static <T> Iterator<T> convertStreamProviderToIterator(StreamProvider<T> sp, Exchange exchange) throws IOException {
        return sp.getStream().iterator();
    }

}
