/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.camel.dataformat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import org.squonk.util.IOUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.IOHelper;

/**
 * DataFormat that handles marshaling/unmarshaling of MoleculeObjects to/from JSON.
 * @author timbo
 */
public class StreamingIteratorJsonDataFormat<T> implements DataFormat {

    private static final Logger LOG = Logger.getLogger(StreamingIteratorJsonDataFormat.class.getName());

    private final Class<T> type;
    /**
     * Should the input and output when marshaling be closed when processing is
     * complete. Default is true
     */
    private boolean autoCloseAfterMarshal = true;
    private JsonFactory factory;
    
    private int marshalCount = 0;

    public int getMarshalCount() {
        return marshalCount;
    }

    private int unmarshalCount = 0;

    public int getUnmarshalCount() {
        return unmarshalCount;
    }

    public StreamingIteratorJsonDataFormat(Class<T> type, boolean autoCloseAfterMarshal) {
        this.type = type;
        this.autoCloseAfterMarshal = autoCloseAfterMarshal;
        this.factory = new MappingJsonFactory();
    }

    /**
     * Creates new DataFormat with autoCloseAfterMarshal set to true
     *
     * @param type
     */
    public StreamingIteratorJsonDataFormat(Class<T> type) {
        this(type, true);
    }

    /**
     * Takes an input that must be a an Iterable<T>, an Iterable<T> or a Stream<T> and writes
     * it to the OutputStream. The Input and OutputStream are closed once
     * processing is complete if the autoCloseAfterMarshal field is set to true,
     * otherwise both must be closed by the caller.
     *
     * @param exchange
     * @param obj
     * @param stream
     * @throws IOException
     */
    @Override
    public void marshal(Exchange exchange, Object obj, OutputStream stream) throws IOException {
        Iterator<T> iter = null;
        if (obj instanceof Iterator) {
            iter = (Iterator<T>) obj;
        } else if (obj instanceof Iterable) {
            iter = ((Iterable<T>) obj).iterator();
        } else if (obj instanceof Stream) {
            iter = ((Stream<T>) obj).iterator();
        } else {
            throw new IllegalArgumentException("Can't handle type of " + obj.getClass().getName());
        }

        JsonGenerator generator = factory.createGenerator(stream);
        generator.writeStartArray();
        while (iter.hasNext()) {
            generator.writeObject(iter.next());
            marshalCount++;
        }

        generator.writeEndArray();
        generator.flush();
        
        if (autoCloseAfterMarshal) {
            IOHelper.close(stream);
            IOUtils.closeIfCloseable(obj);
        }
    }

    /**
     * Generate an Iterator of objects of type T from the JSON stream. NOTE: as
     * we can't tell when the processing is finished the Iterator that is
     * returned implements Closeable so that it can be closed by the caller when
     * finished to ensure the underlying stream is closed. Alternatively the
     * caller can close the InputStream directly.
     *
     * @param exchange
     * @param stream
     * @return An Iterator that also implements java.ioCloseable
     * @throws IOException
     */
    @Override
    public Object unmarshal(Exchange exchange, InputStream stream) throws IOException {
        return new JsonIterator(stream);
    }

    class JsonIterator implements Iterator<T>, Closeable {

        List<T> next;
        JsonParser jp;
        InputStream stream;
        boolean finished = false;

        JsonIterator(InputStream stream) throws IOException {
            this.stream = stream;
            this.jp = factory.createParser(stream);
            JsonToken current = jp.nextToken();
            if (current != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Error: root should be array: quiting.");
            }
        }

        private boolean readNext() {
            try {
                if (jp.nextToken() == JsonToken.END_ARRAY) {
                    next = null;
                    finished = true;
                    return false;
                } else {
                    T result = (T) jp.readValueAs(StreamingIteratorJsonDataFormat.this.type);
                    //System.out.println("Read: " + result);
                    next = Collections.singletonList(result);
                    
                }
                return true;
            } catch (IOException ex) {
                throw new RuntimeException("Failed to read", ex);
            }
        }

        @Override
        public boolean hasNext() {
            if (finished == true) {
                return false;
            } else if (next != null) {
                return true;
            } else {
                return readNext();
            }
        }

        @Override
        public T next() {
            if (next == null) {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more data");
                }
            }
            T result = next.get(0);
            next = null;
            unmarshalCount++;
            return result;
        }

        @Override
        public void close() throws IOException {
            stream.close();
            jp.close();
        }

    }
}
