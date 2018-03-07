/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.MoleculeObject;
import org.squonk.types.NumberRange;
import org.squonk.util.CommonConstants;
import org.squonk.util.Metrics;
import org.squonk.util.NumberTransform;
import org.squonk.util.StatsRecorder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Processor that can be used to transform and aggregate properties of a Stream of BasicObjects.
 * Typically used for multi-parameter optimisation (MPO) scores.
 * All maths and comparisons are done as double values, though you can specify that the score is returned as a float.
 */
public class MpoAccumulatorProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(MpoAccumulatorProcessor.class.getName());

    public enum AccumulatorStrategy {SUM, MEAN}

    private final String accumulatedPropertyName;
    private final String accumulatedPropertyDescription;
    private final Class accumulatedPropertyType;
    private final AccumulatorStrategy accumulatorStrategy;
    private final boolean allowNullValues;
    private final String filterModeProperty;
    private final String filterRangeProperty;
    private final Map<String, NumberTransform> functions = new LinkedHashMap<>();

    /**
     *
     * @param accumulatedPropertyName The name of the property that will hold the aggregated score.
     * @param accumulatedPropertyDescription The description of the property that will hold the aggregated score.
     * @param accumulatedPropertyType Float or Double.
     * @param filterModeProperty The name of the header property that contains the filter mode. Must be one of the
     *                           CommonConstants.VALUE_INCLUDE_* values.
     * @param filterRangeProperty The name  of the header property that contains the filter values. Must be parsable by
     *                            the #NumberRange.Double(String) constructor.
     * @param accumulatorStrategy Strategy for accumulating the values.
     * @param allowNullValues When accumulating generate a score when any of the contributing values is not defined.
     */
    public MpoAccumulatorProcessor(
            String accumulatedPropertyName,
            String accumulatedPropertyDescription,
            Class accumulatedPropertyType,
            String filterModeProperty,
            String filterRangeProperty,
            AccumulatorStrategy accumulatorStrategy,
            boolean allowNullValues) {
        this.accumulatedPropertyName = accumulatedPropertyName;
        this.accumulatedPropertyDescription = accumulatedPropertyDescription;
        this.accumulatedPropertyType = accumulatedPropertyType;
        this.accumulatorStrategy = accumulatorStrategy;
        this.allowNullValues = allowNullValues;
        this.filterModeProperty = filterModeProperty;
        this.filterRangeProperty = filterRangeProperty;
    }

    public MpoAccumulatorProcessor(
            String accumulatedPropertyName,
            String accumulatedPropertyDescription,
            Class accumulatedPropertyType,
            String filterModeProperty,
            String filterRangeProperty) {
        this(accumulatedPropertyName, accumulatedPropertyDescription, accumulatedPropertyType, filterModeProperty, filterRangeProperty, AccumulatorStrategy.SUM, false);
    }

    public MpoAccumulatorProcessor(
            String accumulatedPropertyName,
            String accumulatedPropertyDescription,
            Class accumulatedPropertyType) {
        this(accumulatedPropertyName, accumulatedPropertyDescription, accumulatedPropertyType, null, null, AccumulatorStrategy.SUM, false);
    }


    @Override
    public void process(Exchange exch) throws Exception {

        exch.getIn().getHeaders().forEach((k, v) -> LOG.info("Header " + k + " -> " + v));

        LOG.info("Processing " + functions.size() + " MPO functions. AllowNullValues=" + allowNullValues);


        Dataset<MoleculeObject> ds = exch.getIn().getBody(Dataset.class);

        // generate the mpo score
        AtomicInteger count = new AtomicInteger(0);
        Stream<MoleculeObject> stream = ds.getStream().peek((mo) -> {
            processMoleculeObject(mo);
            count.incrementAndGet();
        });

        // apply filter if necessary
        if (filterModeProperty != null && filterRangeProperty != null) {
            String filterMode = exch.getIn().getHeader(filterModeProperty, String.class);
            String filterRange = exch.getIn().getHeader(filterRangeProperty, String.class);
            if (filterMode != null && !CommonConstants.VALUE_INCLUDE_ALL.equals(filterMode)) {
                Double minScore = null;
                Double maxScore = null;

                if (filterMode != null && filterRange != null) {
                    NumberRange.Double range = new NumberRange.Double(filterRange);
                    minScore = range.getMinValue() == null ? null : range.getMinValue().doubleValue();
                    maxScore = range.getMaxValue() == null ? null : range.getMaxValue().doubleValue();

                    LOG.info("Filter: " + filterMode + " [" + minScore + " - " + maxScore + "]");
                    if (minScore != null || maxScore != null) {
                        final String fm = filterMode;
                        final Double miv = minScore;
                        final Double mav = maxScore;
                        stream = stream.filter((MoleculeObject mo) -> filter(mo, fm, miv, mav));
                    }
                }
            }
        }
        // convert from double to float if needed
        if (accumulatedPropertyType == Float.class) {
            stream = stream.peek((mo) -> {
                Double d = mo.getValue(accumulatedPropertyName, Double.class);
                if (d != null) {
                    mo.putValue(accumulatedPropertyName, d.floatValue());
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
        meta.createField(accumulatedPropertyName, Metrics.PROVIDER_SQUONK, accumulatedPropertyDescription, accumulatedPropertyType);

        MoleculeObjectDataset neu = new MoleculeObjectDataset(stream, meta);
        exch.getOut().setBody(neu);
    }

    protected boolean filter(MoleculeObject mo, String filterMode, Double minScore, Double maxScore) {
        Double result = mo.getValue(accumulatedPropertyName, Double.class);
        if (filterMode == null || CommonConstants.VALUE_INCLUDE_ALL.equals(filterMode)) {
            return true;
        } else {
            if (result == null) {
                return false;
            }
            boolean inRange = false;
            if ((minScore == null || minScore <= result.doubleValue()) &&
                    (maxScore == null || maxScore >= result.doubleValue())) {
                inRange = true;
            }
            //LOG.finer("Filtering: " + result.doubleValue() + " -> " + inRange);
            if ((CommonConstants.VALUE_INCLUDE_PASS.equals(filterMode) && inRange) ||
                    (CommonConstants.VALUE_INCLUDE_FAIL.equals(filterMode) && !inRange)) {
                return true;
            }
        }
        return false;
    }

    protected void processMoleculeObject(MoleculeObject mo) {
        final AtomicDouble sum = new AtomicDouble(0);
        final AtomicInteger count = new AtomicInteger(0);
        functions.entrySet().forEach((e) -> {
            Object o = mo.getValue(e.getKey());
            if (o != null && o instanceof Number) {
                LOG.finer("Adding property " + e.getKey() + ": " + o);
                Number n = (Number) o;
                Double score = e.getValue().transform(n.doubleValue());
                if (score != null) {
                    sum.addAndGet(score);
                    count.incrementAndGet();
                }
            } else {
                LOG.info("Property " + e.getKey() + " not present or not a number: " + o);
            }
        });
        if (allowNullValues || count.get() == functions.size()) {
            LOG.finer("Calculating MPO score");
            Double result = null;
            switch (accumulatorStrategy) {
                case SUM:
                    result = sum.get();
                    break;
                case MEAN:
                    result = sum.doubleValue() / count.doubleValue();
                    break;
            }
            if (result != null) {
                mo.putValue(accumulatedPropertyName, result);
            }
        } else {
            LOG.info("Unable to calculate MPO score " + accumulatedPropertyName + " because of missing input value(s)");
        }
    }


    public MpoAccumulatorProcessor addHumpFunction(String propertyName, NumberTransform humpFunction) {
        functions.put(propertyName, humpFunction);
        return this;
    }
}
