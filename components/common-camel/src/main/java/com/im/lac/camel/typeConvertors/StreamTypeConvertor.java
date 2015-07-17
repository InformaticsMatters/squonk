package com.im.lac.camel.typeConvertors;

import com.im.lac.util.StreamProvider;
import java.util.Iterator;
import java.util.stream.Stream;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
@Converter
public class StreamTypeConvertor {

    @Converter
    public static Iterator convertToIterator(Stream s, Exchange exchange) {
        return s.iterator();
    }
    
    @Converter
    public static Iterator convertToIterator(StreamProvider sp, Exchange exchange) {
        return sp.getStream().iterator();
    }
}
