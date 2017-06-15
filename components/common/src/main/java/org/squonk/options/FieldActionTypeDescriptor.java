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

package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/** Defines a set of action mappings for a set of input fields.
 * Currently actions are simple (e.g. add, delete) but this will need to support additional parameters e.g.
 * for an "rename" action we need to be able able to define that when this action is selected an additional element should
 * be present that allows to specify the new name
 *
 * Created by timbo on 03/02/16.
 */
public class FieldActionTypeDescriptor extends SimpleTypeDescriptor<FieldActionMapping> {

    private final String[] actions;

    public FieldActionTypeDescriptor(@JsonProperty("actions") String[] actions) {
        super(FieldActionMapping.class);
        this.actions = actions;
    }

    public String[] getActions() {
        return actions;
    }

}
