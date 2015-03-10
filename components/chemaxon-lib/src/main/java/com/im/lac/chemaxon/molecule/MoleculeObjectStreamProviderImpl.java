package com.im.lac.chemaxon.molecule;

import com.im.lac.types.MoleculeObject;
import com.im.lac.util.MoleculeObjectStreamGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation using ChemAxon MRecordReader
 *
 * @author timbo
 */
public class MoleculeObjectStreamProviderImpl implements MoleculeObjectStreamGenerator {

    private final InputStream input;

    public MoleculeObjectStreamProviderImpl(InputStream input) {
        this.input = input;
    }

    @Override
    public Stream<MoleculeObject> getStream() throws IOException {
        return getStream(true);
    }

    @Override
    public Stream<MoleculeObject> getStream(boolean parallel, int batchSize) throws IOException {
        Spliterator spliterator = new MoleculeObjectSpliterator(input, batchSize);
        return createStream(spliterator, parallel);
    }

    @Override
    public Stream<MoleculeObject> getStream(boolean parallel) throws IOException {
        Spliterator spliterator = new MoleculeObjectSpliterator(input);
        return createStream(spliterator, parallel);
    }

    private Stream<MoleculeObject> createStream(Spliterator spliterator, boolean parallel) {
        Stream<MoleculeObject> stream = StreamSupport.stream(spliterator, parallel);
        return stream.onClose(() -> {
            try {
                input.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

}
