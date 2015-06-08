package com.im.lac.types.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Unmarshaller for reading MoleculeObject JSON.
 * Designed for one-off use, or at least use where the meta data is always the
 * same.
 *
 * @author timbo
 */
public class MoleculeObjectUnmarshaler {

    private final Metadata meta;
    private final ObjectMapper mapper;

    public MoleculeObjectUnmarshaler(Metadata meta) {
        if (meta == null) {
            throw new NullPointerException("metadata must not be null");
        }
        this.meta = meta;

        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(MoleculeObject.class, new MoleculeObjectJsonDeserializer());
        mapper.registerModule(module);
    }

    public Metadata getMeta() {
        return meta;
    }

    Stream<MoleculeObject> streamFromJson(InputStream in) throws IOException {
        Iterator<MoleculeObject> iter = iteratorFromJson(in);
        Spliterator<MoleculeObject> spliterator = Spliterators.spliterator(iter, meta.getSize(),
                Spliterator.NONNULL | Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, true);
    }

    Iterator<MoleculeObject> iteratorFromJson(InputStream in) throws IOException {
        ContextAttributes attrs = ContextAttributes.getEmpty()
                .withSharedAttribute(MoleculeObjectJsonDeserializer.ATTR_MAPPINGS, meta.getPropertyTypes());
        ObjectReader reader = mapper.reader(MoleculeObject.class).with(attrs);
        Iterator<MoleculeObject> mols = reader.readValues(in);
        return mols;
    }

}
