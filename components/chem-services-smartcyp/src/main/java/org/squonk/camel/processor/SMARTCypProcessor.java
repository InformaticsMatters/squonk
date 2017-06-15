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

package org.squonk.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.smartcyp.SMARTCypRunner;
import org.squonk.types.AtomPropertySet;
import org.squonk.types.MoleculeObject;
import org.squonk.types.Scales;
import org.squonk.util.StatsRecorder;

import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 30/09/16.
 */
public class SMARTCypProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(SMARTCypProcessor.class.getName());

    @Override
    @SuppressWarnings("unchecked")
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
            //meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_GEN, DatasetMetadata.PROP_RANGE, runner.getGeneralRange());
            meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_GEN, DatasetMetadata.PROP_SCALE, Scales.SMARTCyp);
        }
        if (runner.isPerform2D6()) {
            meta.createField(SMARTCypRunner.FIELD_NAME_2D6, source, "Cytochrome 2D6 metabolism prediction", AtomPropertySet.class);
            //meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_2D6, DatasetMetadata.PROP_RANGE, runner.get2D6Range());
            meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_2D6, DatasetMetadata.PROP_SCALE, Scales.SMARTCyp);
        }
        if (runner.isPerform2C9()) {
            meta.createField(SMARTCypRunner.FIELD_NAME_2C9, source, "Cytochrome 2C9 metabolism prediction", AtomPropertySet.class);
            //meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_2C9, DatasetMetadata.PROP_RANGE, runner.get2C9Range());
            meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_2C9, DatasetMetadata.PROP_SCALE, Scales.SMARTCyp);
        }

        return meta;
    }

}
