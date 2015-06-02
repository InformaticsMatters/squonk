package com.im.lac.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.im.lac.types.MoleculeObject;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author timbo
 */
class MoleculeObjectJsonSpliterator extends Spliterators.AbstractSpliterator<MoleculeObject> implements Closeable {
    
    private static final Logger LOG = Logger.getLogger(MoleculeObjectJsonSpliterator.class.getName());

    private List<MoleculeObject> next;
    private final JsonFactory factory;
    private final InputStream input;
    private JsonParser jp;
    private boolean finished = false;
    private int unmarshalCount = 0;

   
    MoleculeObjectJsonSpliterator(JsonFactory factory, InputStream input) throws IOException {
        super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
        this.factory = factory;
        this.input = input;
        this.jp = factory.createParser(input);
        JsonToken current = jp.nextToken();
        if (current != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Error: root should be array, found " + current + ": quiting.");
        }
    }
    
     public int getUnmarshalCount() {
        return unmarshalCount;
    }


    Stream asStream(boolean parallel) {
        Stream<MoleculeObject> stream = StreamSupport.stream(this, parallel);
        return stream.onClose(() -> {
            try {
                this.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public void close() throws IOException {
        LOG.log(Level.FINER, "Closing stream {0}", input);
        input.close();
        jp.close();
    }

    @Override
    public boolean tryAdvance(Consumer<? super MoleculeObject> action) {
        if (hasNext()) {
            action.accept(next());
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<MoleculeObject> trySplit() {
        return super.trySplit();
    }

    private boolean readNext() {
        try {
            if (jp.nextToken() == JsonToken.END_ARRAY) {
                next = null;
                finished = true;
                return false;
            } else {
                MoleculeObject result = jp.readValueAs(MoleculeObject.class);
                next = Collections.singletonList(result);
            }
            return true;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read", ex);
        }
    }

    private boolean hasNext() {
        if (finished == true) {
            return false;
        } else if (next != null) {
            return true;
        } else {
            return readNext();
        }
    }

    private MoleculeObject next() {
        if (next == null) {
            if (!hasNext()) {
                throw new NoSuchElementException("No more data");
            }
        }
        MoleculeObject result = next.get(0);
        next = null;
        unmarshalCount++;
        return result;
    }
    
}
