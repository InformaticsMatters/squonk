/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

import org.apache.camel.TypeConverter;
import org.squonk.execution.steps.AbstractStep;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/** Simple step used for testing that reads and integer and input and writes its string value as the output
 *
 * Created by timbo on 06/01/16.
 */
public class IntegerToStringStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(IntegerToStringStep.class.getName());

    @Override
    public Map<String, Object> doExecute(Map<String, Object> inputs) throws Exception {
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("Must be a single input");
        }
        Object input = inputs.values().iterator().next();
        TypeConverter converter = findTypeConverter();
        Integer result;
        if (converter == null) {
            result = new Integer(input.toString());
        } else {
            result = converter.convertTo(Integer.class, input);
        }
        return Collections.singletonMap("output", result);
    }

}
