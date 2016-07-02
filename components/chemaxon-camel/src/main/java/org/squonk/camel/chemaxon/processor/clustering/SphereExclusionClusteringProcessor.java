package org.squonk.camel.chemaxon.processor.clustering;

import com.chemaxon.descriptors.common.Descriptor;
import com.chemaxon.descriptors.common.DescriptorComparator;
import com.chemaxon.descriptors.common.DescriptorGenerator;
import org.squonk.camel.chemaxon.processor.ProcessorUtils;
import org.squonk.chemaxon.clustering.SphereExclusionClusterer;
import org.squonk.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.util.ExecutionStats;
import org.squonk.util.StatsRecorder;

/**
 * @author timbo
 */
public class SphereExclusionClusteringProcessor<T extends Descriptor> implements Processor {

    private static final Logger LOG = Logger.getLogger(SphereExclusionClusteringProcessor.class.getName());

    public static final String HEADER_MIN_CLUSTER_COUNT = "min_clusters";
    public static final String HEADER_MAX_CLUSTER_COUNT = "max_clusters";

    private String clusterPropertyName;
    private int minClusterCount = SphereExclusionClusterer.DEFAULT_MIN_CLUSTER_COUNT;
    private int maxClusterCount = SphereExclusionClusterer.DEFAULT_MAX_CLUSTER_COUNT;
    private final DescriptorGenerator<T> generator;
    private final DescriptorComparator<T> comparator;

    public SphereExclusionClusteringProcessor(
            DescriptorGenerator<T> generator,
            DescriptorComparator<T> comparator) {
        this.generator = generator;
        this.comparator = comparator;
    }

    public SphereExclusionClusteringProcessor(
            DescriptorGenerator<T> generator,
            DescriptorComparator<T> comparator,
            int minClusterCount,
            int maxClusterCount) {
        this(generator, comparator);
        this.minClusterCount = minClusterCount;
        this.maxClusterCount = maxClusterCount;
    }

    public SphereExclusionClusteringProcessor clusterPropertyName(String propName) {
        this.clusterPropertyName = propName;
        return this;
    }

    public SphereExclusionClusteringProcessor minClusterCount(int count) {
        this.minClusterCount = count;
        return this;
    }

    public SphereExclusionClusteringProcessor maxClusterCount(int count) {
        this.maxClusterCount = count;
        return this;
    }

    @Override
    public void process(Exchange exch) throws Exception {

        SphereExclusionClusterer clusterer = createClusterer(exch);

        Dataset dataset = exch.getIn().getBody(Dataset.class);
        if (dataset == null || dataset.getType() != MoleculeObject.class) {
            throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
        }

        Stream<MoleculeObject> results = null;
        try (Stream<MoleculeObject> mols = dataset.getStream()) {
            results = clusterer.clusterMoleculeObjects(mols);
        }
        MoleculeObjectDataset output = new MoleculeObjectDataset(results);
        int count = output.getDataset().getItems().size();
        //LOG.info("Putting " + count + " clustering results: " + dataset);
        exch.getIn().setBody(output);
        StatsRecorder recorder = exch.getIn().getHeader(StatsRecorder.HEADER_STATS_RECORDER, StatsRecorder.class);
        if (recorder != null) {
            Map<String,Integer> stats = new HashMap<>();
            ExecutionStats.increment(stats, "Cluster_CXN", count);
            recorder.recordStats(stats);
        }
    }

    SphereExclusionClusterer createClusterer(Exchange exchange) {
        SphereExclusionClusterer clusterer = new SphereExclusionClusterer(generator, comparator);
        if (clusterPropertyName != null) {
            clusterer.setClusterPropertyName(clusterPropertyName);
        }
        clusterer.setMinClusterCount(ProcessorUtils.determineIntProperty(exchange, minClusterCount, HEADER_MIN_CLUSTER_COUNT));
        clusterer.setMaxClusterCount(ProcessorUtils.determineIntProperty(exchange, maxClusterCount, HEADER_MAX_CLUSTER_COUNT));

        return clusterer;
    }

}
