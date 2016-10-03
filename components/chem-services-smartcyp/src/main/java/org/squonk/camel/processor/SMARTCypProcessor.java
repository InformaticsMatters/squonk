package org.squonk.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.property.MoleculeCalculator;
import org.squonk.smartcyp.SMARTCypRunner;
import org.squonk.types.AtomPropertySet;
import org.squonk.types.MoleculeObject;
import org.squonk.util.StatsRecorder;

import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 30/09/16.
 */
public class SMARTCypProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(SMARTCypProcessor.class.getName());

    @Override
    public void process(Exchange exch) throws Exception {
        Dataset<MoleculeObject> dataset = exch.getIn().getBody(Dataset.class);
        if (dataset == null || dataset.getType() != MoleculeObject.class) {
            throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
        }

        SMARTCypRunner runner = new SMARTCypRunner(exch.getIn().getHeaders());
        Stream<MoleculeObject> results = runner.execute(dataset.getStream());

        StatsRecorder recorder = exch.getIn().getHeader(StatsRecorder.HEADER_STATS_RECORDER, StatsRecorder.class);
        if (recorder != null) {
            results = results.onClose(() -> {
                recorder.recordStats(runner.getExecutionStats());
            });
        }
        DatasetMetadata meta = handleMetadata(exch, dataset.getMetadata(), runner);
        exch.getIn().setBody(new MoleculeObjectDataset(results, meta));
    }

    protected DatasetMetadata handleMetadata(Exchange exch, DatasetMetadata meta, SMARTCypRunner runner) throws IllegalAccessException, InstantiationException {
        if (meta == null) {
            meta = new DatasetMetadata(MoleculeObject.class);
        }
        String source = "SMARTCyp 2.4.2";
        if (runner.isPerformGeneral()) {
            meta.createField(SMARTCypRunner.FIELD_NAME_GEN, source, "General P450 metabolism prediction", AtomPropertySet.class);
        }
        if (runner.isPerform2D6()) {
            meta.createField(SMARTCypRunner.FIELD_NAME_2D6, source, "Cytochrome 2D6 metabolism prediction", AtomPropertySet.class);
        }
        if (runner.isPerform2C9()) {
            meta.createField(SMARTCypRunner.FIELD_NAME_2C9, source, "Cytochrome 2C9 metabolism prediction", AtomPropertySet.class);
        }

        return meta;
    }

}
