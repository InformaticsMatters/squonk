package com.im.lac.util;

import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * Simple stream provider that is just a wrapper around an existing stream.
 *
 * @author timbo
 */
public class SimpleMoleculeObjectStreamProvider implements MoleculeObjectStreamProvider {
    
    final Stream<MoleculeObject> stream;
    
    public SimpleMoleculeObjectStreamProvider(Stream<MoleculeObject> stream) {
        this.stream = stream;
    }

    @Override
    public Stream<MoleculeObject> getStream() throws IOException {
        return stream;
    }
    
}
