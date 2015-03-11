package com.im.lac.util;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Simple stream provider that is just a wrapper around an existing stream.
 *
 * @author timbo
 */
public class SimpleStreamProvider<T> implements StreamProvider {
    
    final private Stream<T> stream;
    final private Class<T> type;
    
    public SimpleStreamProvider(Stream<T> stream, Class<T> type) {
        this.stream = stream;
        this.type = type;
    }

    @Override
    public Stream<T> getStream() throws IOException {
        return stream;
    }

    @Override
    public Class getType() {
        return type;
    }
    
}
