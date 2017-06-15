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

import chemaxon.marvin.io.MRecord;
import chemaxon.struc.MProp;
import chemaxon.struc.MPropertyContainer;
import org.squonk.types.MoleculeObject;
import org.squonk.types.MoleculeObjectIterable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 *
 * @author timbo
 */
class MoleculeObjectFactory implements MoleculeObjectIterable, Iterator<MoleculeObject>, Closeable {
    InputStream is;
    MRecordIterator iter;

    MoleculeObjectFactory(InputStream is) throws IOException {
        this.is = is;
        this.iter = new MRecordIterator(is);
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public MoleculeObject next() {
        //long t0 = System.nanoTime();
        MRecord rec = iter.next();
        String mol = rec.getString();
        String format = rec.getInputFormat();
        if (format == null) {
            format = iter.getRecordReader().getRecognizedFormat();
        }
        if (format != null) {
            // this is a BIG HACK because Marvin does not always give you the right answer
            // TODO - work out something better
            if (format.startsWith("smiles")) {
                format = "smiles";
            } else if (format.startsWith("smarts")) {
                format = "smarts";
            } else if (format.startsWith("mol")) {
                format = "mol";
            } else if (format.startsWith("sdf")) {
                format = "mol"; // yes, this is supposed to be mol
            } else if (format.startsWith("inchi")) {
                format = "inchi";
            } else if (format.startsWith("mrv")) {
                format = "mrv";
            } else {
                format = null;
            }
        }
        String name = rec.getMoleculeName();
        MoleculeObject mo = new MoleculeObject(mol, format);
        if (name != null && name.length() > 0) {
            mo.putValue("name", name);
        }
        MPropertyContainer pc = rec.getPropertyContainer();
        String[] keys = pc.getKeys();
        for (int x = 0; x < keys.length; x++) {
            String key = keys[x];
            MProp prop = pc.get(key);
            mo.putValue(key, prop.getPropValue());
        }
        //long t1 = System.nanoTime();
        //System.out.println("Reading took: " + (t1-t0));
        return mo;
    }

    @Override
    public Iterator<MoleculeObject> iterator() {
        return this;
    }

    @Override
    public void close() throws IOException {
        iter.close();
    }
    
}
