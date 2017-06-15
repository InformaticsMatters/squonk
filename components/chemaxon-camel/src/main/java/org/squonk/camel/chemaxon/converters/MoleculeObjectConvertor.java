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

package org.squonk.camel.chemaxon.converters;

import org.squonk.chemaxon.molecule.MoleculeObjectUtils;
import org.squonk.types.MoleculeObject;
import org.squonk.types.MoleculeObjectIterable;
import org.squonk.util.StreamProvider;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.SDFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;

/**
 * Created by timbo on 21/04/2014.
 */
@Converter
public class MoleculeObjectConvertor {

    @Converter
    public static MoleculeObject createMoleculeObjectFromString(String mol, Exchange exchange) {
        return new MoleculeObject(mol);
    }

    /**
     * Create an Iterable of MoleculeObjects from an InputStream. IMPORTANT:
     * whilst the implementation type is not defined here it is certain that the
     * underlying stream will need to be closed. To do this check whether the
     * MoleculeObjectIterable that is returned implements the Closeable
     * interface and if so call the close method once you have finished
     * iterating. Ideally we should have an implementation that is independent
     * of Marvin as other chemistry implementations will need to depend on
     * ChemAxon even if this is the only thing they need.
     *
     * @param is
     * @param exchange
     * @return
     * @throws IOException
     */
    @Converter
    public static MoleculeObjectIterable createMoleculeObjectIterable(InputStream is, Exchange exchange)
            throws IOException {
        return MoleculeObjectUtils.createIterable(is);
    }

    /**
     * Create an Iterable of MoleculeObjects from an File. See the InputStream
     * variant for more details.
     *
     * @param file
     * @param exchange
     * @return
     * @throws java.io.IOException
     */
    @Converter
    public static MoleculeObjectIterable createMoleculeObjectIterable(File file, Exchange exchange) throws IOException {
        return MoleculeObjectUtils.createIterable(file);
    }
    
    @Converter
    public static StreamProvider createMoleculeObjectStream(InputStream is, Exchange exchange) throws IOException {
        return MoleculeObjectUtils.createStreamGenerator(is);
    }
    
    @Converter
    public static StreamProvider createMoleculeObjectStream(SDFile sdf, Exchange exchange) throws IOException {
        return MoleculeObjectUtils.createStreamGenerator(sdf.getInputStream());
    }
    
    @Converter
    public static StreamProvider createMoleculeObjectStream(File file, Exchange exchange) throws IOException {
        return MoleculeObjectUtils.createStreamGenerator(new FileInputStream(file));
    }
    
    @Converter
    public static MoleculeObjectDataset createMoleculeObjectDataset(SDFile sdf, Exchange exchange) throws IOException {
        StreamProvider sp = createMoleculeObjectStream(sdf, exchange);
        MoleculeObjectDataset mods = new MoleculeObjectDataset(sp.getStream());
        // TODO - what about the name?
        return mods;
    }


}
