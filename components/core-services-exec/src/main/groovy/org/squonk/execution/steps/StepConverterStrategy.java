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

package org.squonk.execution.steps;

import org.squonk.io.IODescriptor;

import java.util.List;

/**
 * Created by timbo on 17/06/17.
 */
public interface StepConverterStrategy {

    /**
     * Add any converter steps that are needed to convert input to output
     *
     * @param producerId The cell ID
     * @param jobId      The job ID
     * @param from       The source IODescriptors
     * @param to         The destination IODescriptors
     * @param steps      The steps that might need converters
     * @return           The full set of steps with converters interspersed as necessary
     */
    public List<Step> addConverterSteps(
            Long producerId, String jobId,
            IODescriptor[] from, List<Step> steps, IODescriptor[] to);
}
