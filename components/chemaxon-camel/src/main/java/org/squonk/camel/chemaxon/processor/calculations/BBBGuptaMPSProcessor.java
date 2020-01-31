/*
 * Copyright (c) 2020 Informatics Matters Ltd.
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
package org.squonk.camel.chemaxon.processor.calculations;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.squonk.chemaxon.molecule.BBBGuptaMPSCalculator;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.MoleculeObject;
import org.squonk.util.Metrics;
import org.squonk.util.StatsRecorder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class BBBGuptaMPSProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(BBBGuptaMPSProcessor.class.getName());


    @Override
    public void process(Exchange exch) throws Exception {
        Message msg = exch.getIn();
        final BBBGuptaMPSCalculator calculator = new BBBGuptaMPSCalculator();
        MoleculeObjectDataset dataset = msg.getBody(MoleculeObjectDataset.class);
        DatasetMetadata meta = dataset.getMetadata();
        calculator.updateMetadata(meta);
        Stream<MoleculeObject> stream1 = dataset.getStream();

        AtomicInteger count = new AtomicInteger(0);
        StatsRecorder recorder = exch.getIn().getHeader(StatsRecorder.HEADER_STATS_RECORDER, StatsRecorder.class);
        if (recorder != null) {
            stream1 = stream1.onClose(() -> {
                int total = count.get();
                recorder.recordStats(Metrics.METRICS_MPO, total);
                LOG.fine("Recording stats " + Metrics.METRICS_MPO + ":" + total);
            });
        }

        Stream stream2 = stream1.peek((mo) -> {
            calculator.calculate(mo);
            count.incrementAndGet();
        });

        msg.setBody(new MoleculeObjectDataset(stream2, meta));
    }
}
