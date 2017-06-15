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

package org.squonk.rdkit.io;

import org.RDKit.MolDraw2DSVG;
import org.RDKit.ROMol;
import org.squonk.io.AbstractMolDepict;
import org.squonk.io.DepictionParameters;
import org.squonk.rdkit.mol.MolReader;

import java.awt.image.BufferedImage;

/**
 * Created by timbo on 24/01/2016.
 */
public class RdkitMolDepict extends AbstractMolDepict<ROMol> {


    public RdkitMolDepict() {
        super();
    }

    public RdkitMolDepict(DepictionParameters params) {
        super(params);
    }

    @Override
    public ROMol smilesToMolecule(String smiles) throws Exception {
        return MolReader.generateMolFromSmiles(smiles);
    }

    @Override
    public ROMol v2000ToMolecule(String molecule) throws Exception {
        return MolReader.generateMolFromMolfile(molecule);
    }

    @Override
    public ROMol v3000ToMolecule(String molecule) throws Exception {
        return MolReader.generateMolFromMolfile(molecule);
    }

    @Override
    public ROMol stringToMolecule(String molecule) throws Exception {
        return MolReader.generateMolFromString(molecule, null);
    }

    @Override
    public BufferedImage moleculeToImage(ROMol molecule, DepictionParameters params) throws Exception {
        return null;
    }

    @Override
    public String moleculeToSVG(ROMol molecule, DepictionParameters params) throws Exception {
        DepictionParameters p = depictionParameters(params);
        MolDraw2DSVG o = new MolDraw2DSVG(p.getWidth(), p.getHeight());
        o.drawMolecule(molecule);
        return o.getDrawingText();
    }
}
