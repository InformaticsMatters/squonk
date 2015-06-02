package com.im.lac.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class MoleculeObjectUnmarshaler {

    private final Metadata meta;
    private final JsonFactory factory = new MappingJsonFactory();
    
    
    public MoleculeObjectUnmarshaler(Metadata meta) {
        this.meta = meta;
    }


    public Metadata getMeta() {
        return meta;
    }

    Stream<MoleculeObject> read(InputStream in) throws IOException {
        MoleculeObjectJsonSpliterator s = new MoleculeObjectJsonSpliterator(factory, in);
        return s.asStream(true);
    }

}
