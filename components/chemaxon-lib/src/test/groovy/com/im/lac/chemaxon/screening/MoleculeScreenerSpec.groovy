package com.im.lac.chemaxon.screening

import chemaxon.formats.MolImporter
import chemaxon.standardizer.Standardizer
import chemaxon.struc.Molecule
import chemaxon.struc.MoleculeGraph
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpGenerator
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpParameters
import spock.lang.Shared
import spock.lang.Specification

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
