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

package org.squonk.chemaxon.screening

import chemaxon.formats.MolImporter
import chemaxon.standardizer.Standardizer
import chemaxon.struc.Molecule
import chemaxon.struc.MoleculeGraph
import chemaxon.util.standardizer.StandardizerUtil
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpGenerator
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpParameters
import spock.lang.Shared
import spock.lang.Specification

import spock.lang.IgnoreIf

/**
 *
 * @author Tim Dudgeon
 */
class MoleculeScreenerSpec extends Specification {
    
    @Shared def mols = [
        "NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O",
        "CN(C)C1=C(Cl)C(=O)C2=C(C=CC=C2)C1=O",
        "CN(C)C1=C(Cl)C(=O)C2=C(C=CC=C2)C1=O.Cl"
    ]

    void "test read default standardizer"() {
        String xml = StandardizerUtil.DEFAULT_STANDARDIZER_CONFIG
        println xml

        when:
        Standardizer szr = new Standardizer(xml)

        then:
        szr != null
    }

    @IgnoreIf({ System.getenv('CHEMAXON_LICENCE_ABSENT') != null })
    void "test identical"() {
        setup:
        EcfpParameters params = EcfpParameters.createNewBuilder().build();
        EcfpGenerator generator = params.getDescriptorGenerator();
        MoleculeScreener screener = new MoleculeScreener(generator, generator.getDefaultComparator());
        Molecule mol = MolImporter.importMol(mols[0])
       
        when:
        double d = screener.compare(mol, mol)
        
        then:
        d == 1.0
    }

    @IgnoreIf({ System.getenv('CHEMAXON_LICENCE_ABSENT') != null })
    void "test different"() {
        setup:
        EcfpParameters params = EcfpParameters.createNewBuilder().build();
        EcfpGenerator generator = params.getDescriptorGenerator();
        MoleculeScreener screener = new MoleculeScreener(generator, generator.getDefaultComparator());
        Molecule mol0 = MolImporter.importMol(mols[0])
        Molecule mol1 = MolImporter.importMol(mols[1])
       
        when:
        double d = screener.compare(mol0, mol1)
        
        then:
        d > 0d
        d < 1d
    }

    @IgnoreIf({ System.getenv('CHEMAXON_LICENCE_ABSENT') != null })
    void "test no standardizer"() {
        setup:
        EcfpParameters params = EcfpParameters.createNewBuilder().build();
        EcfpGenerator generator = params.getDescriptorGenerator();
        MoleculeScreener screener = new MoleculeScreener(generator, generator.getDefaultComparator());
        screener.setStandardizer(null)
        Molecule mol0 = MolImporter.importMol(mols[1])
        Molecule mol1 = MolImporter.importMol(mols[2])
       
        when:
        double d = screener.compare(mol0, mol1)
        
        then:
        d < 1d
    }

    @IgnoreIf({ System.getenv('CHEMAXON_LICENCE_ABSENT') != null })
    void "test custom standardizer"() {
        setup:
        EcfpParameters params = EcfpParameters.createNewBuilder().build();
        EcfpGenerator generator = params.getDescriptorGenerator();
        MoleculeScreener screener = new MoleculeScreener(generator, generator.getDefaultComparator());
        screener.setStandardizer("clearisotopes") // no aromatize
        Molecule mol0 = MolImporter.importMol(mols[1])
        Molecule mol1 = MolImporter.importMol(mols[1])
        mol1.aromatize(MoleculeGraph.AROM_GENERAL)
       
        when:
        double d0 = screener.compare(mol0, mol1)
        double d1 = screener.compare(mol0, mol0)
        
        then:
        d0 < 1d
        d1 == 1d
    }
    
}

