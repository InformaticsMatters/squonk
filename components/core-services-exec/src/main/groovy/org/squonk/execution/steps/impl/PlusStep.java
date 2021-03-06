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

import org.squonk.execution.steps.AbstractStep;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/** Simple step used for testing that reads integer input and adds value to it
 *
 * Created by timbo on 06/01/16.
 */
public class PlusStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(PlusStep.class.getName());

    @Override
    public Map<String, Object> doExecute(Map<String, Object> inputs) throws Exception {
        int toAdd = getOption("add", Integer.class, 0);
        Integer value = (Integer)inputs.get("input");
        int result = value + toAdd;
        usageStats.put("Plus", 1);
        statusMessage = value + " + " + toAdd + " = " + result;
        return Collections.singletonMap("output", result);
    }
}
