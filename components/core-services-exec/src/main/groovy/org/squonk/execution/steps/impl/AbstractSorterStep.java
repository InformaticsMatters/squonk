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

package org.squonk.execution.steps.impl;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.types.BasicObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractSorterStep<P extends BasicObject> extends AbstractDatasetStep<P,P> {

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;


    protected Stream<P> doSort(Stream<P> input, List<SortDirective> sortDirectives) {

        Stream<P> sorted = input.sorted(new SortComparator(sortDirectives));
        sorted = addStreamCounter(sorted, MSG_PROCESSED);

        return sorted;
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
