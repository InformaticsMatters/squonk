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

package org.squonk.execution.steps.impl;

import groovy.lang.GroovyClassLoader;
import org.apache.camel.TypeConverter;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.types.BasicObject;

import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 29/12/15.
 */
public class DatasetFilterGroovyStep<P extends BasicObject> extends AbstractDatasetStandardStep<P,P> {

    private static final Logger LOG = Logger.getLogger(DatasetFilterGroovyStep.class.getName());

    public static final String OPTION_SCRIPT = StepDefinitionConstants.TrustedGroovyDataset.OPTION_SCRIPT;

    protected Dataset<P> doExecute(Dataset<P> input, Map<String,Object> options, TypeConverter converter) throws Exception {

        String script = getOption(options, OPTION_SCRIPT, String.class, converter);
        if (script == null) {
            throw new IllegalStateException("Script not defined. Should be present as option named " + OPTION_SCRIPT);
        }
        LOG.info("Script: " + script);

        GroovyClassLoader gcl = new GroovyClassLoader();
        String clsDef = buildClassDefinition(script);
        LOG.info("Built predicate class:\n" + clsDef);
        Class<Predicate> cls = gcl.parseClass(clsDef);
        Predicate predicate = cls.newInstance();
        statusMessage = "Filtering ...";
        Stream output = input.getStream().filter(predicate);
        Dataset<P> results = new Dataset(output, deriveOutputDatasetMetadata(input.getMetadata()));
        statusMessage = generateStatusMessage(input.getSize(), results.getSize(), -1);
        return results;
    }


    protected DatasetMetadata<P> deriveOutputDatasetMetadata(DatasetMetadata<P> input) {
        if (input == null) {
            return new DatasetMetadata(BasicObject.class);
        } else {
            return new DatasetMetadata(input.getType(), input.getValueClassMappings(), 0, input.getProperties());
        }
    }

    private String buildClassDefinition(String script) {
        return "class Filter implements java.util.function.Predicate {\n  boolean test(def bo) {\n    bo.values.with {      \n" +
                script + "\n}}}";
    }
}
