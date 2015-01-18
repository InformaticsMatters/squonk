package com.im.lac.chemaxon.screening;

import chemaxon.struc.Molecule;
import com.chemaxon.descriptors.common.Descriptor;
import com.chemaxon.descriptors.common.DescriptorComparator;
import com.chemaxon.descriptors.common.DescriptorGenerator;
import com.im.lac.chemaxon.molecule.MoleculeUtils;

/**
 * Allows molecules to be screened based on similarity using a variety of
 * ChemAxon molecular descriptors. Needs to be provided with a descriptor
 * generator and a comparator that would typically be generated using the
 * ChemAxon API along these lines:
 * <br>
 * <code>
 * EcfpParameters params = EcfpParameters.createNewBuilder().build();
 * EcfpGenerator generator = params.getDescriptorGenerator();
 * MoleculeScreener screener = new MoleculeScreener(generator, generator.getDefaultComparator());
 * </code>
 * <br>
 * The most complex part is the creation of the generator and comparator.
 * Consult the ChemAxon API documentation for full details on this. There is
 * support for chemical hashed fingerprints, ECFP fingerprints, 2D pharmacophore
 * fingerprints and 3D shape descriptors. Once the instance has been created
 * with its generator and comparator usage should be straight forward.
 * <br>
 * Note: the ChemAxon API that is used for this class is not yet stable, so this
 * class may change in the future.
 *
 * @author Tim Dudgeon
 * @param <T> The type of descriptor
 */
public class MoleculeScreener<T extends Descriptor> {

    private final DescriptorGenerator<T> generator;
    private final DescriptorComparator<T> comparator;
    private T targetFp;

    public MoleculeScreener(DescriptorGenerator<T> generator, DescriptorComparator<T> comparator) {
        this.generator = generator;
        this.comparator = comparator;
    }

    /**
     * Set the target molecule. This molecule will be used when using the
     * #compare(Molecule) method
     *
     * @param mol
     */
    public void setTargetMol(Molecule mol) {
        targetFp = generateDescriptor(mol);
    }
    
    public T getTargetDescriptor() {
        return targetFp;
    }
    
    public T generateDescriptor(Molecule mol) {
        return generator.generateDescriptor(prepareMolecule(mol));
    }

    /**
     * Compare these two molecules using the descriptor and return the
     * similarity score
     *
     * @param mol1
     * @param mol2
     * @return
     */
    public double compare(Molecule mol1, Molecule mol2) {
        return comparator.calculateSimilarity(
                generateDescriptor(mol1),
                generateDescriptor(mol2));
    }

    /**
     * Compare this molecule to the target molecule using the descriptor and
     * return the similarity score
     *
     * @param query
     * @return
     */
    public double compare(Molecule query) {
        return comparator.calculateSimilarity(targetFp, generateDescriptor(query));
    }

    public double compare(T query, T target) {
        return comparator.calculateSimilarity(query, target);
    }
    
    public double compare(Molecule query, T target) {
        return comparator.calculateSimilarity(generateDescriptor(query), target);
    }
    
    /** Prepares the molecule for descriptor generation. Not clear what is the best 
     * approach here, and multiple strategies might be needed. Current approach is 
     * to use the largest fragment, thought this may not always be what's needed.
     * The user can pre-process the molecules beforehand if necessary. 
     * 
     * 
     * @param mol
     * @return 
     */
    Molecule prepareMolecule(Molecule mol) {
        mol = MoleculeUtils.findParentStructure(mol);
        // what about hydrogens and aromatisation?
        return mol;
    }
    
}
