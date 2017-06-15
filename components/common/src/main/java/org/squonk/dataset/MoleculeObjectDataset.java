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

package org.squonk.dataset;

import org.squonk.types.MoleculeObject;
import org.squonk.util.StreamProvider;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * MoleculeObject specific wrapper for Dataset to get round problem with generics. e.g. you can say
 * MoleculeObjectDataset.class but you can't say Dataset&lt;MoleculeObject&gt;.class
 * This class exposes a few of the most useful methods from the Dataset it wraps, but for the full
 * functionality using the {@link #getDataset} method
 *
 *
 * @author timbo
 */
public class MoleculeObjectDataset implements DatasetProvider, StreamProvider {

    private final Dataset<MoleculeObject> mods;

    public MoleculeObjectDataset(Dataset<MoleculeObject> mods) {
        this.mods = mods;
    }

    public MoleculeObjectDataset(Stream<MoleculeObject> stream, DatasetMetadata meta) {
        this.mods = new Dataset(MoleculeObject.class, stream, meta);
    }

    public MoleculeObjectDataset(Stream<MoleculeObject> stream) {
        this.mods = new Dataset(MoleculeObject.class, stream);
    }
    
    public MoleculeObjectDataset(Collection<MoleculeObject> objs, DatasetMetadata meta) {
        this.mods = new Dataset(MoleculeObject.class, objs, meta);
    }

    public MoleculeObjectDataset(Collection<MoleculeObject> objs) {
        this.mods = new Dataset(MoleculeObject.class, objs);
    }

    @Override
    public Dataset<MoleculeObject> getDataset() {
        return mods;
    }

    @Override
    public DatasetMetadata getMetadata() throws Exception {
        return mods.getMetadata();
    }

    /** Get the items from the wrapped dataset as a List.
     * See comments in {@link Dataset} for concerns about scalability for large datasets.
     * 
     * @return
     * @throws IOException 
     */
    public List<MoleculeObject> getItems() throws IOException {
        return mods.getItems();
    }

    /** Get items from the wrapped dataset as a Stream. 
     * See comments in {@link Dataset} about possibly only being able to do this once.
     * 
     * @return
     * @throws IOException 
     */
    @Override
    public Stream<MoleculeObject> getStream() throws IOException {
        return mods.getStream();
    }

    @Override
    public Class getType() {
        return MoleculeObject.class;
    }



}
