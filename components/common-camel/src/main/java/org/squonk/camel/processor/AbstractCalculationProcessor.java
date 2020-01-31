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

package org.squonk.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.MoleculeObject;
import org.squonk.types.NumberRange;
import org.squonk.util.CommonConstants;
import org.squonk.util.Metrics;
import org.squonk.util.StatsRecorder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Processor that can be used to calculate a property for a Stream of BasicObjects .
 * Typically used for performing a calculation from previously predicted properties.
 * All maths and comparisons are done as double values, though you can specify that the score is returned as a float.
 */
public abstract class AbstractCalculationProcessor implements Processor {

    protected static final Logger LOG = Logger.getLogger(AbstractCalculationProcessor.class.getName());


    protected final String calculatedPropertyName;
    private final String calculatedPropertyDescription;
    private final Class calculatedPropertyType;
    private final String filterModeProperty;
    private final String filterRangeProperty;


    /**
     *
     * @param calculatedPropertyName The name of the property that will hold the aggregated score.
     * @param calculatedPropertyDescription The description of the property that will hold the aggregated score.
     * @param calculatedPropertyType Float or Double.
     * @param filterModeProperty The name of the header property that contains the filter mode. Must be one of the
     *                           CommonConstants.VALUE_INCLUDE_* values.
     * @param filterRangeProperty The name  of the header property that contains the filter values. Must be parsable by
     *                            the #NumberRange.Double(String) constructor.
     */
    public AbstractCalculationProcessor(
            String calculatedPropertyName,
            String calculatedPropertyDescription,
            Class calculatedPropertyType,
            String filterModeProperty,
            String filterRangeProperty) {
        this.calculatedPropertyName = calculatedPropertyName;
        this.calculatedPropertyDescription = calculatedPropertyDescription;
        this.calculatedPropertyType = calculatedPropertyType;
        this.filterModeProperty = filterModeProperty;
        this.filterRangeProperty = filterRangeProperty;
    }

    @Override
    public void process(Exchange exch) throws Exception {

        LOG.info("Processing calculation" + calculatedPropertyName + " AllowNullValues=" +
                ", type=" + calculatedPropertyType.getName());

        Dataset<MoleculeObject> ds = exch.getIn().getBody(Dataset.class);

        // generate the mpo score
        AtomicInteger count = new AtomicInteger(0);
        Stream<MoleculeObject> stream = ds.getStream().peek((mo) -> {
            processMoleculeObject(mo);
            count.incrementAndGet();
        });

        // apply filter if necessary
        stream = MpoAccumulatorProcessor.applyFilters(exch.getIn(), stream, calculatedPropertyName,
                filterModeProperty, filterRangeProperty);

        // convert from double to float if needed
        if (calculatedPropertyType == Float.class) {
            stream = stream.peek((mo) -> {
                Double d = mo.getValue(calculatedPropertyName, Double.class);
                if (d != null) {
                    mo.putValue(calculatedPropertyName, d.floatValue());
                }
            });
        }

        // Record the stats
        StatsRecorder recorder = exch.getIn().getHeader(StatsRecorder.HEADER_STATS_RECORDER, StatsRecorder.class);
        if (recorder != null) {
            stream = stream.onClose(() -> {
                int total = count.get();
                recorder.recordStats(Metrics.METRICS_MPO, total);
                LOG.fine("Recording stats " + Metrics.METRICS_MPO + ":" + total);
            });
        }

        // update the metadata
        DatasetMetadata meta = ds.getMetadata();
        meta.createField(calculatedPropertyName, Metrics.PROVIDER_SQUONK, calculatedPropertyDescription, calculatedPropertyType);

        MoleculeObjectDataset neu = new MoleculeObjectDataset(stream, meta);
        exch.getOut().setBody(neu);
    }

    /**
     * Perform the calculation. Subclasses must define this.
     * @param mo
     */
    protected abstract void processMoleculeObject(MoleculeObject mo);


}
