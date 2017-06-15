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

import org.squonk.types.MoleculeObjectIterable;
import org.squonk.util.StreamGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Utilities for MoleculeObjects
 *
 * @author timbo
 */
public class MoleculeObjectUtils {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectUtils.class.getName());

    /**
     * Uses MRecordReader. Does not create any {@link chemaxon.struc.Molecule} instances
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static MoleculeObjectIterable createIterable(InputStream is) throws IOException {
        return new MoleculeObjectFactory(is);
    }

    /**
     * Uses MRecordReader. Does not create any {@link chemaxon.struc.Molecule} instances
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static MoleculeObjectIterable createIterable(File file) throws IOException {
        return new MoleculeObjectFactory(new FileInputStream(file));
    }

    /**
     * Generates a provider of Stream&lt;MoleculeObject&gt;.
     * Uses MRecordReader. Does not create any {@link chemaxon.struc.Molecule} instances
     * 
     * @param is The input stream. Either close this or the Stream once finished.
     * @return
     * @throws IOException 
     */
    public static StreamGenerator createStreamGenerator(InputStream is) throws IOException {
        return new MoleculeObjectStreamProviderImpl(is);
    }
   
}
