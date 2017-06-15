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

package org.squonk.chemaxon.molecule;

import org.squonk.types.MoleculeObject;
import org.squonk.util.StreamGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation using ChemAxon MRecordReader
 *
 * @author timbo
 */
public class MoleculeObjectStreamProviderImpl implements StreamGenerator<MoleculeObject> {

    private final InputStream input;

    public MoleculeObjectStreamProviderImpl(InputStream input) {
        this.input = input;
    }
    
     @Override
    public Class<MoleculeObject> getType() {
        return MoleculeObject.class;
    }


    @Override
    public Stream<MoleculeObject> getStream() {
        return getStream(true);
    }

    @Override
    public Stream<MoleculeObject> getStream(boolean parallel, int batchSize) {
        MoleculeObjectSpliterator spliterator = new MoleculeObjectSpliterator(input, batchSize);
        return createStream(spliterator, parallel);
    }

    @Override
    public Stream<MoleculeObject> getStream(boolean parallel) {
        MoleculeObjectSpliterator spliterator = new MoleculeObjectSpliterator(input);
        return createStream(spliterator, parallel);
    }

    private Stream<MoleculeObject> createStream(MoleculeObjectSpliterator spliterator, boolean parallel) {
        Stream<MoleculeObject> stream = StreamSupport.stream(spliterator, parallel);
        return stream.onClose(() -> {
            try {
                spliterator.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
   
}
