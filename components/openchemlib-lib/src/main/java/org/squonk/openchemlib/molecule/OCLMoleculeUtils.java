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

package org.squonk.openchemlib.molecule;

import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import org.squonk.types.MoleculeObject;

import java.util.logging.Logger;

/** Utilies class for handling OpenChemLib molecules.
 *
 * IMPORTANT: OpenChemLib (or at least SmilesParser) does not appear to be thread safe. The importXxx() methods here use
 * synchronization to accommodate this, but if using the library directly use accordingly.
 *
 * Created by timbo on 05/04/16.
 */
public class OCLMoleculeUtils {

    private static final Logger LOG = Logger.getLogger(OCLMoleculeUtils.class.getName());
    public static final SmilesParser SMILES_PARSER = new SmilesParser();
    public static final MolfileParser MOLFILE_PARSER = new MolfileParser();


    public static StereoMolecule importString(String molecule, String format) {
        if (format != null)
            try {
                if (format.toLowerCase().equals("smiles") || format.toLowerCase().startsWith("smiles:")) {
                    return importSmiles(molecule);
                } else if (format.toLowerCase().equals("mol") || format.toLowerCase().startsWith("mol:")) {
                    return importMolfile(molecule);
                }
            } catch (Exception ex) {
                LOG.fine("Failed to parse molecule " + ex.getMessage());
                return null;
            }
        try {
            return importMolfile(molecule);
        } catch (Exception ex1) {
            try {
                return importSmiles(molecule);
            } catch (Exception ex2) {
                LOG.info("Failed to parse molecule as mol or smiles");
                return null;
            }
        }
    }

    public static StereoMolecule importSmiles(String smiles) throws Exception {
        StereoMolecule mol = new StereoMolecule();
        synchronized (SMILES_PARSER) {
            SMILES_PARSER.parse(mol, smiles);
        }
        return mol;
    }

    public static StereoMolecule importMolfile(String molfile) throws Exception {
        StereoMolecule mol = new StereoMolecule();
        synchronized (MOLFILE_PARSER) {
            MOLFILE_PARSER.parse(mol, molfile);
        }
        return mol;
    }

    public static StereoMolecule fetchMolecule(MoleculeObject mo, boolean store) {
        StereoMolecule mol = importString(mo.getSource(), mo.getFormat());
        if (store && mol != null) {
            mo.putRepresentation(StereoMolecule.class.getName(), mol);
        }
        return mol;
    }

}
