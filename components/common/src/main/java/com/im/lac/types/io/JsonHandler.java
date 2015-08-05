package com.im.lac.types.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.im.lac.dataset.Metadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author timbo
 */
public class JsonHandler {

    private static final Logger LOG = Logger.getLogger(JsonHandler.class.getName());

    public static final String ATTR_METADATA = "metadata";

    private final ObjectMapper mapper;

    public JsonHandler() {

        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        // TODO - better way to register custom deserializers
        module.addDeserializer(MoleculeObject.class, new MoleculeObjectJsonDeserializer());
        module.addSerializer(MoleculeObject.class, new MoleculeObjectJsonSerializer());
        mapper.registerModule(module);
    }

    public Object unmarshalItem(Metadata meta, InputStream in) throws ClassNotFoundException, IOException {
        Class cls = Class.forName(meta.getClassName());
        return doUnmarshalItem(cls, meta, in);
    }

    private <T> T doUnmarshalItem(Class<T> cls, Metadata meta, InputStream in) throws IOException {
        ContextAttributes attrs = ContextAttributes.getEmpty()
                .withSharedAttribute(ATTR_METADATA, meta);
        ObjectReader reader = mapper.reader(cls).with(attrs);
        T result = reader.readValue(in);
        return result;
    }

    public Iterator<? extends Object> unmarshalItemsAsIterator(Metadata meta, InputStream in) throws IOException, ClassNotFoundException {
        Class cls = Class.forName(meta.getClassName());
        return doUnmarshalItemsAsIterator(cls, meta, in);
    }

    public Stream<? extends Object> unmarshalItemsAsStream(Metadata meta, InputStream in) throws IOException, ClassNotFoundException {
        Iterator<? extends Object> iter = unmarshalItemsAsIterator(meta, in);
        int size = meta.getSize();
        Spliterator<? extends Object> spliterator;
        if (size == 0) {
            spliterator = Spliterators.spliteratorUnknownSize(iter, Spliterator.NONNULL | Spliterator.ORDERED);
        } else {
            spliterator = Spliterators.spliterator(iter, size, Spliterator.NONNULL | Spliterator.ORDERED);
        }
        return StreamSupport.stream(spliterator, true);
    }

    private <T> Iterator<T> doUnmarshalItemsAsIterator(Class<T> cls, Metadata meta, InputStream in) throws IOException {
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(ATTR_METADATA, meta);
        ObjectReader reader = mapper.reader(cls).with(attrs);
        Iterator<T> result = reader.readValues(in);
        return result;
    }

    public void marshalItem(Object item, Metadata meta, OutputStream outputStream) throws IOException {
        LOG.fine("marshalling item to JSON");
        meta.setClassName(item.getClass().getName());
        meta.setType(Metadata.Type.ITEM);
        meta.setSize(1);
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(ATTR_METADATA, meta);
        ObjectWriter writer = mapper.writerFor(item.getClass()).with(attrs);
        writer.writeValue(outputStream, item);
    }

    public String marshalItemAsString(Object item, Metadata meta) throws IOException {
        LOG.fine("marshalling item to JSON");
        meta.setClassName(item.getClass().getName());
        meta.setType(Metadata.Type.ITEM);
        meta.setSize(1);
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(ATTR_METADATA, meta);
        ObjectWriter writer = mapper.writerFor(item.getClass()).with(attrs);
        return writer.writeValueAsString(item);
    }

    public byte[] marshalItemAsBytes(Object item, Metadata meta) throws IOException {
        LOG.fine("marshalling item to JSON");
        meta.setClassName(item.getClass().getName());
        meta.setType(Metadata.Type.ITEM);
        meta.setSize(1);
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(ATTR_METADATA, meta);
        ObjectWriter writer = mapper.writerFor(item.getClass()).with(attrs);
        return writer.writeValueAsBytes(item);
    }

    public void marshalItems(Stream items, Metadata meta, OutputStream outputStream) throws IOException {
        LOG.fine("marshalling items to JSON");
        meta.setType(Metadata.Type.ARRAY);
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("metadata", meta);
        ObjectWriter ow = mapper.writer().with(attrs);
        try (SequenceWriter sw = ow.writeValuesAsArray(outputStream)) {

            final AtomicReference<Class> classNameRef = new AtomicReference<>();
            long count = ((Stream) items.sequential()).peek((i) -> {
                try {
                    if (classNameRef.get() == null) {
                        LOG.log(Level.FINE, "Setting type to {0}", i.getClass());
                        classNameRef.set(i.getClass());
                    } else {
                        if (classNameRef.get() != i.getClass()) {
                            throw new IllegalStateException("Items must all be of the same type. Previous: " + classNameRef.get().getName() + " Current: " + i.getClass().getName());
                        }
                    }
                    sw.write(i);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to write object: " + i, ex);
                }
            }).count();
            meta.setSize(count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count);
            if (count > 0) {
                meta.setClassName(classNameRef.get().getName());
            }

        } finally {
            outputStream.close();
        }

    }

    public String objectToJson(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public <T> T objectFromJson(InputStream is, Class<T> type) throws IOException {
        return mapper.readValue(is, type);
    }

    public <T> T objectFromJson(InputStream is, Class<T> type, Metadata meta) throws IOException {
        ObjectReader reader = mapper.reader(type).withAttribute(ATTR_METADATA, meta);
        return reader.readValue(is);
    }

    public <T> T objectFromJson(String s, Class<T> type) throws IOException {
        return mapper.readValue(s, type);
    }

    public <T> T objectFromJson(String s, Class<T> type, Metadata meta) throws IOException {
        ObjectReader reader = mapper.reader(type).withAttribute(ATTR_METADATA, meta);
        return reader.readValue(s);
    }

    public <T> Iterator<T> iteratorFromJson(String s, Class<T> type) throws IOException {
        ObjectReader reader = mapper.reader(type);
        return reader.readValues(s);
    }

    public <T> Iterator<T> iteratorFromJson(String s, Class<T> type, Metadata meta) throws IOException {
        ObjectReader reader = mapper.reader(type).withAttribute(ATTR_METADATA, meta);
        return reader.readValues(s);
    }

    public <T> Iterator<T> iteratorFromJson(InputStream is, Class<T> type, Metadata meta) throws IOException {
        ObjectReader reader = mapper.reader(type).withAttribute(ATTR_METADATA, meta);
        return reader.readValues(is);
    }

    public <T> Iterator<T> iteratorFromJson(InputStream is, Class<T> type) throws IOException {
        ObjectReader reader = mapper.reader(type);
        return reader.readValues(is);
    }

    public <T> Stream<T> streamFromJson(final InputStream is, final Class<T> type, final boolean autoClose) throws IOException {
        return streamFromJson(is, type, null, autoClose);
    }

    public <T> Stream<T> streamFromJson(final InputStream is, final Class<T> type, Metadata meta, final boolean autoClose) throws IOException {
        ObjectReader reader = mapper.reader(type);
        if (meta != null) {
            reader = reader.withAttribute(ATTR_METADATA, meta);
        }
        Iterator<T> iter = reader.readValues(is);
        Spliterator spliterator = Spliterators.spliteratorUnknownSize(iter, Spliterator.NONNULL | Spliterator.ORDERED);
        Stream<T> stream = StreamSupport.stream(spliterator, true);
        if (autoClose) {
            return stream.onClose(() -> IOUtils.close(is));
        } else {
            return stream;
        }
    }

}
