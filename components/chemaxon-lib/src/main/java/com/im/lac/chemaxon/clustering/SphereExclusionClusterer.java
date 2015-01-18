package com.im.lac.chemaxon.clustering;

import chemaxon.calculations.hydrogenize.Hydrogenize;
import chemaxon.struc.Molecule;
import com.chemaxon.calculations.common.ProgressObservers;
import com.chemaxon.clustering.common.IDBasedSingleLevelClustering;
import com.chemaxon.clustering.common.MolInput;
import com.chemaxon.clustering.common.MolInputBuilder;
import com.chemaxon.clustering.sphex.SphereExclusion;
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpComparator;
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpGenerator;
import com.chemaxon.descriptors.fingerprints.ecfp.EcfpParameters;
import com.chemaxon.descriptors.metrics.BinaryMetrics;
import com.im.lac.chemaxon.molecule.MoleculeIterable;
import com.im.lac.chemaxon.molecule.SimpleMoleculeIterable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author timbo
 */
public class SphereExclusionClusterer {

    private static final String PROP_NAME_ORIGINAL_MOLECULE = "__ORIGINAL_MOLECULE__";

    /**
     * The name of the property that holds the cluster ID. Default is "cluster"
     */
    private String clusterPropertyName = "cluster";

    /**
     * The initial minimum number of expected clusters.
     *
     */
    private int minClusterCount = 5;

    /**
     * The maximum number of expected clusters.
     *
     */
    private int maxClusterCount = 10;

    public String getClusterPropertyName() {
        return clusterPropertyName;
    }

    public void setClusterPropertyName(String propName) {
        this.clusterPropertyName = propName;
    }

    public int getMinClusterCount() {
        return minClusterCount;
    }

    public void setMinClusterCount(int minClusterCount) {
        this.minClusterCount = minClusterCount;
    }

    public int getMaxClusterCount() {
        return maxClusterCount;
    }

    public void setMaxClusterCount(int maxClusterCount) {
        this.maxClusterCount = maxClusterCount;
    }

    public MoleculeIterable cluster(MoleculeIterable mols) {

        MolInputBuilder inputBuilder = prepareMolecules(mols);
        MolInput input = generateInput(inputBuilder);
        IDBasedSingleLevelClustering clus = SphereExclusion.adaptiveSPHEX(
                minClusterCount,
                maxClusterCount,
                input,
                ProgressObservers.createForgivingNullObserver());

        return generateOutput(input, clus);
    }

    MolInputBuilder prepareMolecules(MoleculeIterable mols) {
        final MolInputBuilder inputBuilder = new MolInputBuilder();
        for (Molecule mol : mols) {
            // work with a clone so we don't destroy the original
            Molecule clone = mol.cloneMolecule();
            // Keep the largest fragment
            final Molecule[] frags = clone.convertToFrags();
            Molecule largestFrag = frags[0];
            for (int i = 1; i < frags.length; i++) {
                if (frags[i].getAtomCount() > largestFrag.getAtomCount()) {
                    largestFrag = frags[i];
                }
            }
            // Aromatize read structure
            largestFrag.aromatize(Molecule.AROM_BASIC);
            // Dehydrogenize read structure
            Hydrogenize.convertExplicitHToImplicit(largestFrag);
            largestFrag.setPropertyObject(PROP_NAME_ORIGINAL_MOLECULE, mol);
            // set the orignal to the working molecule so that we can restore it later
            inputBuilder.addMolecule(largestFrag);
        }
        return inputBuilder;
    }

    MolInput generateInput(MolInputBuilder inputBuilder) {
        final EcfpGenerator gen = (new EcfpParameters()).getDescriptorGenerator();
        final EcfpComparator comp = gen.getBinaryMetricsComparator(BinaryMetrics.BINARY_TANIMOTO);
        // Construct dissimilarity input
        return inputBuilder.build(gen, comp);
    }

    MoleculeIterable generateOutput(MolInput input, IDBasedSingleLevelClustering clus) {
        List<Molecule> results = new ArrayList<Molecule>();
        for (int i = 0; i < input.size(); i++) {
            Molecule orig = (Molecule) input.getMolecule(i).getPropertyObject(PROP_NAME_ORIGINAL_MOLECULE);
            orig.setPropertyObject(clusterPropertyName, clus.clusters().indexOf(clus.clusterOf(i).get()));
            results.add(orig);
        }
        return new SimpleMoleculeIterable(results);
    }
}
