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

package org.squonk.camel.chemaxon.dataformat;

import chemaxon.formats.MolExporter;
import chemaxon.struc.Molecule;
import org.squonk.chemaxon.molecule.MoleculeObjectUtils;
import org.squonk.chemaxon.molecule.MoleculeUtils;
import org.squonk.types.MoleculeObject;
import org.squonk.types.MoleculeObjectIterable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 *
 * @author timbo
 */
public class MoleculeObjectIteratorDataFormat implements DataFormat {
    
    private static final Logger LOG = Logger.getLogger(MoleculeObjectIteratorDataFormat.class.getName());

    @Override
    public void marshal(Exchange exchange, Object o, OutputStream out) throws Exception {
        Iterator<MoleculeObject> mols = null;
        if (o instanceof MoleculeObjectIterable) {
            mols = ((MoleculeObjectIterable) o).iterator();
        } else if (o instanceof Iterator) {
            mols = (Iterator<MoleculeObject>) o;
        } else if (o instanceof Iterable) {
            mols = ((Iterable) o).iterator();
        } else if (o instanceof MoleculeObject) {
            mols = Collections.singletonList((MoleculeObject)o).iterator();
        } else {
            throw new IllegalArgumentException("Bade format. Can't handle " + o.getClass().getName());
        }
        MolExporter exporter = new MolExporter(out, "sdf");
        try {
            while (mols.hasNext()) {
                MoleculeObject mo = mols.next();
                Molecule mol = MoleculeUtils.fetchMolecule(mo, false);
                MoleculeUtils.putPropertiesToMolecule(mo.getValues(), mol);
                exporter.write(mol);
                mol.clearProperties();
            }
        } finally {
            exporter.close();
            if (mols instanceof Closeable) {
            try {
                ((Closeable) mols).close();
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "Failed to close iterator", ioe);
            }
        }
        }
    }

    @Override
    public Object unmarshal(Exchange exchange, InputStream is) throws Exception {
        Stream<MoleculeObject> stream = MoleculeObjectUtils.createStreamGenerator(is).getStream();
        return stream.iterator();
    }

}
