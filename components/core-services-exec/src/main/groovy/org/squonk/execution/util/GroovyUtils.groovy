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

package org.squonk.execution.util

import groovy.text.SimpleTemplateEngine

/**
 * Created by timbo on 04/12/16.
 */
class GroovyUtils {

    static String expandTemplate(def text, def values) {
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate(text).make(values)
        return template.toString()
    }

    static Map<String,String> expandValues(Map<String,String> templates, Map<String,Object> values) {
        def engine = new SimpleTemplateEngine()
        Map<String,String> results = [:]
        templates.forEach { k,v ->
            try {
                String result = engine.createTemplate(v).make(values).toString()
                results[k] = result
            } catch (MissingPropertyException) {
                // no value specified so this property will not be present
            }
        }
        return results
    }
}
