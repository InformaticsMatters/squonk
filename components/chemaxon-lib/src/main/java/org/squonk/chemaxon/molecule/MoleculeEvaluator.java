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

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import org.squonk.types.MoleculeObject;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author timbo
 */
public interface MoleculeEvaluator {
    
    enum Mode {

        Calculate, Filter, Transform
    }

    
    Molecule processMolecule(Molecule mol, Map<String,Integer> stats);
    
    MoleculeObject processMoleculeObject(MoleculeObject mol, Map<String,Integer> stats) throws IOException;
    
    Map<String,Object> getResults(Molecule mol);
    
    Mode getMode();

    String getKey();

    String getMetricsCode();

    String getDescription();

    /** Return the name of the field that is created by execution of this evaluator.
     * This will be null when filtering or transforms are involved
     *
     * @return The property name, which might be null.
     */
    String getPropName();
    
}
