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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.squonk.chemaxon.molecule;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author timbo
 */
class MoleculeFactory implements MoleculeIterable, Closeable {
    final Iterator<Molecule> iterator;
    final MolImporter importer;

    MoleculeFactory(Iterator<Molecule> iterator) {
        this.iterator = iterator;
        importer = null;
    }

    MoleculeFactory(MolImporter importer) {
        this.iterator = importer.iterator();
        this.importer = importer;
    }

    @Override
    public Iterator<Molecule> iterator() {
        return iterator;
    }

    @Override
    public void close() throws IOException {
        if (importer != null) {
            importer.close();
        }
    }
    
}
