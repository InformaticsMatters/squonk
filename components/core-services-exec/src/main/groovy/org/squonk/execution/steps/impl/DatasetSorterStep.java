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

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.types.BasicObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by timbo on 13/09/16.
 */
public class DatasetSorterStep<P extends BasicObject> extends AbstractDatasetStep<P,P> {

    private static final Logger LOG = Logger.getLogger(DatasetSorterStep.class.getName());

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;
    public static final String OPTION_DIRECTIVES = StepDefinitionConstants.DatasetSorter.OPTION_DIRECTIVES;

    @Override
    protected Dataset<P> doExecuteWithDataset(Dataset<P> input, CamelContext camelContext) throws Exception {

        String directivesStr = getOption(OPTION_DIRECTIVES, String.class, findTypeConverter(camelContext));
        if (directivesStr == null) {
            throw new IllegalStateException("Sort directives must be defined as option named " + OPTION_DIRECTIVES);
        }

        DatasetMetadata<P> meta = input.getMetadata();
        List<SortDirective> directives = parse(directivesStr, meta);

        Stream<P> stream = input.getStream();
        Stream<P> sorted = stream.sorted(new SortComparator(directives));

        meta.appendDatasetHistory("Sorted according to " + directives.stream()
                .map((sd) -> sd.field + (sd.ascending ? " ASC" : " DESC"))
                .collect(Collectors.joining(", ")));

        Dataset<P> result = new Dataset(sorted, meta);

        statusMessage = generateStatusMessage(input.getSize(), result.getSize(), -1);

        return result;
    }

    protected List<SortDirective> parse(String string, DatasetMetadata meta) {
        List<SortDirective> list = new ArrayList<>();
        String[] lines = string.split("\n|( *, *)");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            SortDirective sd = null;
            if (parts.length == 1) {
                sd = new SortDirective(parts[0], true);
            } else if (parts.length == 2) {
                if ("ASC".equalsIgnoreCase(parts[1])) {
                    sd = new SortDirective(parts[0], true);
                } else if ("DESC".equalsIgnoreCase(parts[1])) {
                    sd = new SortDirective(parts[0], false);
                } else {
                    throw new IllegalArgumentException("Invalid sort directive. Expected ASC or DESC but found: " + parts[1]);
                }
            } else {
                throw new IllegalArgumentException("Invalid sort expression: " + line);
            }
            if (sd != null) {
                Class type = (Class)meta.getValueClassMappings().get(sd.field);
                if (type == null) {
                    throw new IllegalArgumentException("Invalid field name: " + sd.field);
                }
                if (!Comparable.class.isAssignableFrom(type)) {
                    throw new IllegalArgumentException("Field type is not Comparable: " + type.getName());
                }
                list.add(sd);
            }
        }

        return list;
    }


    class SortDirective {

        String field;
        boolean ascending;

        SortDirective(String field, boolean ascending) {
            this.field = field;
            this.ascending = ascending;
        }

    }

    class SortComparator<T extends BasicObject> implements Comparator<T> {

        private final List<SortDirective> directives;

        SortComparator(List<SortDirective> directives) {
            this.directives = directives;
        }

        @Override
        public int compare(BasicObject o1, BasicObject o2) {
            for (SortDirective directive : directives) {
                Comparable c1 = (Comparable)o1.getValue(directive.field);
                Comparable c2 = (Comparable)o2.getValue(directive.field);
                int outcome = 0;
                if (c1 != null && c2 != null) {
                    outcome = directive.ascending ? c1.compareTo(c2) : 0 - c1.compareTo(c2);
                } else if (c1 != null && c2 == null) {
                    outcome = -1;
                } else if (c1 == null && c2 != null) {
                    outcome = 1;
                }
                if (outcome != 0) {
                    return outcome;
                }
            }
            return 0;
        }
    }
}
