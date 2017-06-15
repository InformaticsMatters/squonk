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

import chemaxon.struc.Molecule;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author timbo
 */
class MoleculeCollectionIterableAdapter implements MoleculeIterable {
    final Collection<Molecule> mols;

    MoleculeCollectionIterableAdapter(Collection<Molecule> mols) {
        this.mols = mols;
    }

    @Override
    public Iterator<Molecule> iterator() {
        return mols.iterator();
    }
    
}
