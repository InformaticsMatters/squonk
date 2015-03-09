package com.im.lac.chemaxon.screening;

import chemaxon.struc.Molecule;
import com.chemaxon.descriptors.common.Descriptor;
import com.chemaxon.descriptors.common.DescriptorComparator;
import com.chemaxon.descriptors.common.DescriptorGenerator;
import com.im.lac.chemaxon.molecule.StandardizerEvaluator;

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
 * Standardization of the molecules is an important aspect. This is done using a
 *
 * @{link chemaxon.standardizer.Standardizer}. By default the following
 * configuration (in action string syntax) is used:
 * removefragment:method=keeplargest..aromatize
 *
 * <br>
 * You can specify your own standardizer, or set it to null to not standardize
 * (in which case you almost certainly need to prepare the molecules before
 * screening). Note: standardization is performed on a clone of the molecule,
 * leaving the original un-modified.
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
    public static final String DEFAULT_STANDARDIZER = "removefragment:method=keeplargest..aromatize";
    private StandardizerEvaluator szr;

    public MoleculeScreener(DescriptorGenerator<T> generator, DescriptorComparator<T> comparator) {
        this.generator = generator;
        this.comparator = comparator;
        this.szr = new StandardizerEvaluator(DEFAULT_STANDARDIZER, 25);
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

    public void setStandardizer(String standardizer) {
        if (standardizer == null || standardizer.trim().length() == 0) {
            szr = null;
        } else {
            this.szr = new StandardizerEvaluator(standardizer, 25);
        }
    }

    public T generateDescriptor(Molecule mol) {
        return generator.generateDescriptor(standardizeMolecule(mol));
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

    /**
     * Prepares the molecule for descriptor generation. The standardizer that is
     * used can be specified. If standardizer is null then the molecule is used
     * "as is". If standardizer is present a clone of the molecule is
     * standardized so that the original molecule is not changed.
     *
     *
     * @param mol
     * @return
     */
    Molecule standardizeMolecule(Molecule mol) {
        if (szr == null) {
            return mol;
        } else {
            Molecule clone = mol.cloneMolecule();
            szr.processMolecule(clone);
            return clone;
        }
    }

}
