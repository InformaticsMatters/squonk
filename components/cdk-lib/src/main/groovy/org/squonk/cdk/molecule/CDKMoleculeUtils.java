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

package org.squonk.cdk.molecule;

import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

/**
 *
 * @author timbo
 */
public class CDKMoleculeUtils {

    public static final String CDK_MOLECULE_WITH_ALL_EXPLICIT_HYDROGENS = "CDK_MOLECULE_WITH_ALL_EXPLICIT_HYDROGENS";
    public static final String CDK_MOLECULE_WITH_ALL_IMPLICIT_HYDROGENS = "CDK_MOLECULE_WITH_ALL_IMPLICIT_HYDROGENS";
    public static final Aromaticity AROMATICITY = new Aromaticity(ElectronDonation.cdk(), Cycles.cdkAromaticSet());

    public static final SilentChemObjectBuilder SILENT_OBJECT_BUILDER = (SilentChemObjectBuilder) SilentChemObjectBuilder.getInstance();
    public static final CDKHydrogenAdder HYDROGEN_ADDER = CDKHydrogenAdder.getInstance(SILENT_OBJECT_BUILDER);

    /** create a clone of the molecule that has explicit hydrogens. 
     * 
     * @param mol Assumed to already be initialized and contain implicit hydrogens
     * @return
     * @throws CDKException
     * @throws CloneNotSupportedException 
     */
    public static IAtomContainer moleculeWithExlicitHydrogens(IAtomContainer mol) throws CDKException, CloneNotSupportedException {
        IAtomContainer clone = mol.clone();
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(clone);
        return clone;
    }

    /** 
     * Perform basic initialiazation of a molecule by:
     * <ol>
     * <li>Percieving atom types and configuring atoms</li>
     * <li>Adding implicit hydrogens</li>
     * <li>Detecting aromaticity</li>
     * </ol>
     * @param mol
     * @throws CDKException 
     */
    public static void initializeMolecule(IAtomContainer mol) throws CDKException {
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
        HYDROGEN_ADDER.addImplicitHydrogens(mol);
        AROMATICITY.apply(mol);
    }

}
