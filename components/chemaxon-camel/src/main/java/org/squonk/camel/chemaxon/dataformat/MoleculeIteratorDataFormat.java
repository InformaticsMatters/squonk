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
import org.squonk.chemaxon.molecule.MoleculeUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 *
 * @author timbo
 */
public class MoleculeIteratorDataFormat implements DataFormat {

    @Override
    public void marshal(Exchange exchange, Object o, OutputStream out) throws Exception {
        Iterator<Molecule> mols = null;
        if (o instanceof Iterator) {
            mols = (Iterator<Molecule>) o;
        } else if (o instanceof Iterable) {
            mols = ((Iterable) o).iterator();
        } else if (o instanceof Molecule) {
            mols = Collections.singletonList((Molecule)o).iterator();
        }
        MolExporter exporter = new MolExporter(out, "sdf");
        try {
            while (mols.hasNext()) {
                exporter.write(mols.next());
            }
        } finally {
            exporter.close();
        }
    }

    @Override
    public Object unmarshal(Exchange exchange, InputStream in) throws Exception {
        Iterator it = MoleculeUtils.createIterable(in).iterator();
        return it;
    }

}
