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

package org.squonk.camel.cdk.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openscience.cdk.CDK;
import org.squonk.cdk.io.CDKMoleculeIOUtils;
import org.squonk.cdk.molecule.DescriptorCalculator;
import org.squonk.cdk.molecule.MolecularDescriptors;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.MoleculeObject;
import org.squonk.util.StatsRecorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author timbo
 */
public class CDKDatasetConvertProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(CDKDatasetConvertProcessor.class.getName());
    public static final String HEADER_MOLECULE_FORMAT ="MoleculeFormat";


    @Override
    public void process(Exchange exch) throws Exception {
        Dataset<MoleculeObject> dataset = exch.getIn().getBody(Dataset.class);
        if (dataset == null || dataset.getType() != MoleculeObject.class) {
            throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
        }
        final String format = exch.getIn().getHeader(HEADER_MOLECULE_FORMAT, String.class);
        if (format == null) {
            throw new IllegalStateException("Format to convert to must be specified with a header named " + HEADER_MOLECULE_FORMAT);
        } else {
            LOG.info("Converting to format " + format);
        }
        Stream<MoleculeObject> stream = dataset.getStream();
        AtomicInteger counter = new AtomicInteger(0);
        Stream<MoleculeObject> mols = CDKMoleculeIOUtils.convertMoleculeObjects(stream, format);
        mols = mols.peek(mo -> counter.incrementAndGet());

        StatsRecorder recorder = exch.getIn().getHeader(StatsRecorder.HEADER_STATS_RECORDER, StatsRecorder.class);
        if (recorder != null) {
            mols = mols.onClose(() -> {
                List<Map<String,Integer>> stats = new ArrayList<>();
                stats.add(Collections.singletonMap(MolecularDescriptors.STATS_PREFIX + ".MolConvert", counter.get()));
                recorder.recordStats(stats);
            });
        }

        DatasetMetadata meta = dataset.getMetadata().clone();
        meta.appendDatasetHistory("Structures converted to format " + format);
        exch.getIn().setBody(new MoleculeObjectDataset(mols, meta));
    }

}
