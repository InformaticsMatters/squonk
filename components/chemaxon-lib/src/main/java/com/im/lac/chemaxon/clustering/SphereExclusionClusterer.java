package com.im.lac.chemaxon.clustering;

import chemaxon.standardizer.Standardizer;
import chemaxon.struc.Molecule;
import com.chemaxon.calculations.common.ProgressObservers;
import com.chemaxon.clustering.common.IDBasedSingleLevelClustering;
import com.chemaxon.clustering.common.MolInput;
import com.chemaxon.clustering.common.MolInputBuilder;
import com.chemaxon.clustering.sphex.SphereExclusion;
import com.chemaxon.descriptors.common.Descriptor;
import com.chemaxon.descriptors.common.DescriptorComparator;
import com.chemaxon.descriptors.common.DescriptorGenerator;
import com.im.lac.ClosableMoleculeObjectQueue;
import com.im.lac.ClosableQueue;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import com.im.lac.types.MoleculeObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class SphereExclusionClusterer<T extends Descriptor> {

    private static final Logger LOG = Logger.getLogger(SphereExclusionClusterer.class.getName());

    private static final String PROP_NAME_ORIGINAL_MOLECULE = "__ORIGINAL_MOLECULE__";
    private static final String PROP_NAME_CLONED_MOLECULE = "__CLONED_MOLECULE__";
    public static final int DEFAULT_MIN_CLUSTER_COUNT = 5;
    public static final int DEFAULT_MAX_CLUSTER_COUNT = 10;

    /**
     * The name of the property that holds the clusterMolecules ID. Default is "clusterMolecules"
     */
    private String clusterPropertyName = "cluster";

    /**
     * The initial minimum number of expected clusters.
     *
     */
    private int minClusterCount = DEFAULT_MIN_CLUSTER_COUNT;

    /**
     * The maximum number of expected clusters.
     *
     */
    private int maxClusterCount = DEFAULT_MAX_CLUSTER_COUNT;

    private Standardizer standardizer = new Standardizer("removefragment:method=keeplargest..aromatize..removeexplicith");

    private DescriptorGenerator<T> descriptorGenerator;
    private DescriptorComparator<T> descriptorComparator;

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

    public Standardizer getStandardizer() {
        return standardizer;
    }

    public void setStandardizer(Standardizer szr) {
        this.standardizer = szr;
    }

    public void setStandardizerConfig(String szr) {
        this.standardizer = new Standardizer(szr);
    }

    public void setDescriptorGenerator(DescriptorGenerator<T> generator) {
        this.descriptorGenerator = generator;
    }

    public void setDescriptorComparator(DescriptorComparator<T> comparator) {
        this.descriptorComparator = comparator;
    }

    public SphereExclusionClusterer() {

    }

    public SphereExclusionClusterer(
            DescriptorGenerator<T> generator,
            DescriptorComparator<T> comparator) {
        this.descriptorGenerator = generator;
        this.descriptorComparator = comparator;
    }

    public SphereExclusionClusterer(
            DescriptorGenerator<T> generator,
            DescriptorComparator<T> comparator,
            int minClusterCount,
            int maxClusterCount) {
        this(generator, comparator);
        this.minClusterCount = minClusterCount;
        this.maxClusterCount = maxClusterCount;
    }

    public Iterable<Molecule> clusterMolecules(Iterable<Molecule> mols) {
        LOG.log(Level.INFO, "Clustering with min={0} and max={1}", new Object[]{minClusterCount, maxClusterCount});
        MolInputBuilder inputBuilder = prepareInputBuilderForMolecules(mols);
        MolInput input = generateMolInput(inputBuilder);
        IDBasedSingleLevelClustering clus = SphereExclusion.adaptiveSPHEX(
                minClusterCount,
                maxClusterCount,
                input,
                ProgressObservers.createForgivingNullObserver());

        return generateMoleculeOutput(input, clus);
    }
    
    public Iterable<MoleculeObject> clusterMoleculeObjects(Iterable<MoleculeObject> mols) {
        LOG.log(Level.INFO, "Clustering with min={0} and max={1}", new Object[]{minClusterCount, maxClusterCount});
        MolInputBuilder inputBuilder = prepareInputBuilderForMoleculeObjects(mols);
        MolInput input = generateMolInput(inputBuilder);
        IDBasedSingleLevelClustering clus = SphereExclusion.adaptiveSPHEX(
                minClusterCount,
                maxClusterCount,
                input,
                ProgressObservers.createForgivingNullObserver());

        return generateMoleculeObjectOutput(input, clus);
    }

    MolInputBuilder prepareInputBuilderForMolecules(Iterable<Molecule> mols) {
        final MolInputBuilder inputBuilder = new MolInputBuilder();
        for (Molecule mol : mols) {
            Molecule clone = prepareMolecule(mol);
            inputBuilder.addMolecule(clone);
        }
        return inputBuilder;
    }
    
    MolInputBuilder prepareInputBuilderForMoleculeObjects(Iterable<MoleculeObject> mols) {
        final MolInputBuilder inputBuilder = new MolInputBuilder();
        for (MoleculeObject mo : mols) {
            Molecule clone = prepareMoleculeObject(mo);
            inputBuilder.addMolecule(clone);
        }
        return inputBuilder;
    }


    Molecule prepareMolecule(Molecule mol) {
        // work with a clone so we don't destroy the original
        Molecule clone = mol.cloneMolecule();
        standardizer.standardize(clone);
        clone.setPropertyObject(PROP_NAME_ORIGINAL_MOLECULE, mol);
        return clone;
    }
    
    Molecule prepareMoleculeObject(MoleculeObject mo) {
        // work with a clone so we don't destroy the original
        Molecule clone = MoleculeUtils.fetchMolecule(mo, false).cloneMolecule();
        standardizer.standardize(clone);
        clone.setPropertyObject(PROP_NAME_ORIGINAL_MOLECULE, mo);
        return clone;
    }

    MolInput<T> generateMolInput(MolInputBuilder inputBuilder) {
        return inputBuilder.build(descriptorGenerator, descriptorComparator);
    }


    Iterable<Molecule> generateMoleculeOutput(MolInput input, IDBasedSingleLevelClustering clus) {
        final ClosableQueue<Molecule> q = new ClosableQueue<>(50);
        Thread t = new Thread(() -> {
            try {
                for (int i = 0; i < input.size(); i++) {
                    Molecule clone = input.getMolecule(i);
                    Molecule orig = (Molecule) clone.getPropertyObject(PROP_NAME_ORIGINAL_MOLECULE);
                    orig.setPropertyObject(clusterPropertyName, clus.clusters().indexOf(clus.clusterOf(i).get()));
                    clone.clearProperties();
                    q.add(orig);
                }
            } finally {
                q.close();
            }
        });
        t.start();

        return q;
    }
    
    Iterable<MoleculeObject> generateMoleculeObjectOutput(MolInput input, IDBasedSingleLevelClustering clus) {
        final ClosableQueue<MoleculeObject> q = new ClosableMoleculeObjectQueue(50);
        Thread t = new Thread(() -> {
            try {
                for (int i = 0; i < input.size(); i++) {
                    Molecule clone = input.getMolecule(i);
                    MoleculeObject orig = (MoleculeObject) clone.getPropertyObject(PROP_NAME_ORIGINAL_MOLECULE);
                    orig.putValue(clusterPropertyName, clus.clusters().indexOf(clus.clusterOf(i).get()));
                    clone.clearProperties();
                    q.add(orig);
                }
            } finally {
                q.close();
            }
        });
        t.start();

        return q;
    }
}
