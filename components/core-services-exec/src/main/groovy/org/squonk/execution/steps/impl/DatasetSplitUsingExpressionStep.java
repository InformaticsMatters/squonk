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
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Split a dataset based on evaluating a user specified expression which is evaluated against a field's contents.
 * Expressions are specified in the form of 'operator operand. Examples are '< 10', '>=7', '= 10', '= orange'.
 * The supported operators are < <= > >= and =.
 * Records who's values pass the expression get put in the 'pass' group, those that fail in the 'fail' group.
 * Null values are put in the fail group.
 * <p>
 * The entire dataset is held in memory during execution so do not use for very large datasets.
 * <p>
 * Created by timbo on 22/06/2020.
 */
public class DatasetSplitUsingExpressionStep<P extends BasicObject> extends AbstractDatasetSplitStep<P> {

    private static final Logger LOG = Logger.getLogger(DatasetSplitUsingExpressionStep.class.getName());

    public static final String OPTION_FIELD = StepDefinitionConstants.DatasetSplitUsingExpression.OPTION_FIELD;
    public static final String OPTION_EXPR = StepDefinitionConstants.DatasetSplitUsingExpression.OPTION_EXPR;


    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor(
            "core.dataset.splitexpr.v1",
            "DatasetSplitUsingExpression",
            "Split a dataset by evaluating values of a field using an expression",
            new String[]{"filter", "split", "dataset"},
            null, "icons/filter.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            new IODescriptor[]{
                    IODescriptors.createBasicObjectDataset(StepDefinitionConstants.VARIABLE_OUTPUT_PASS),
                    IODescriptors.createBasicObjectDataset(StepDefinitionConstants.VARIABLE_OUTPUT_FAIL)
            },
            new OptionDescriptor[]{
                    new OptionDescriptor<>(new DatasetFieldTypeDescriptor(null),
                            OPTION_FIELD, "Field to evaluate",
                            "Name of field whose values are used to split the dataset",
                            OptionDescriptor.Mode.User),
                    new OptionDescriptor<>(String.class, OPTION_EXPR,
                            "Expression", "Expression to evaluate e.g. < 3.5", OptionDescriptor.Mode.User)
            },
            null, null, null,
            DatasetSplitUsingExpressionStep.class.getName()
    );


    protected Map<String, Object> doExecuteWithDataset(Dataset<P> input) throws Exception {

        TypeConverter converter = findTypeConverter();
        String field = getOption(OPTION_FIELD, String.class, converter);
        String expr = getOption(OPTION_EXPR, String.class, converter);
        if (field == null || field.isEmpty()) {
            throw new IllegalArgumentException("Must specify option " + OPTION_FIELD);
        }
        if (expr == null || expr.isEmpty()) {
            throw new IllegalArgumentException("Must specify option " + OPTION_EXPR);
        } else {
            expr = expr.trim();
        }
        LOG.info("Splitting using expression " + expr + " on field: " + field);
        Class type = input.getMetadata().getValueClassMappings().get(field);
        if (type == null) {
            LOG.info("No metadata for field. Assuming String");
            type = String.class;
        }

        statusMessage = "Splitting ...";
        final Predicate<Comparable> pred = createPredicate(expr, type);
        Map<Boolean, List<P>> groups = input.getStream()
                .collect(Collectors.partitioningBy(mo -> pred.test((Comparable)mo.getValue(field))));

        statusMessage = groups.get(true).size() + " pass and " + groups.get(false).size() + " fail";

        LOG.info("Number pass = " + groups.get(true).size());
        LOG.info("Number fail = " + groups.get(false).size());

        DatasetMetadata passMeta = input.getMetadata().clone();
        DatasetMetadata failMeta = input.getMetadata().clone();

        Map<String, Object> results = new LinkedHashMap(2);
        results.put(StepDefinitionConstants.VARIABLE_OUTPUT_PASS, new Dataset(groups.get(true), passMeta));
        results.put(StepDefinitionConstants.VARIABLE_OUTPUT_FAIL, new Dataset(groups.get(false), failMeta));

        return results;
    }

    Predicate<Comparable> createPredicate(String expr, Class type) {
        Pattern p = Pattern.compile("(<=|>=|<|>|<=|>=|=) *(.*)");
        Matcher m = p.matcher(expr);
        if (m.matches()) {
            String operator = m.group(1);
            String operand = m.group(2);
//            LOG.info("Evaluating " + operator + " " + operand);
            Comparable converted = convertType(operand, type);
//            LOG.info("Converted: " + converted);
            if ("=".equals(operator)) {
                return o -> o == null ? false : o.equals(converted);
            } else if ("<".equals(operator)) {
                return o -> o == null ? false : o.compareTo(converted) < 0;
            } else if ("<=".equals(operator)) {
                return o -> o == null ? false : o.compareTo(converted) <= 0;
            } else if (">".equals(operator)) {
                return o -> o == null ? false : o.compareTo(converted) > 0;
            } else if (">=".equals(operator)) {
                return o -> o == null ? false : o.compareTo(converted) >= 0;
            } else {
                throw new IllegalArgumentException("Operator " + operator + " not supported");
            }

        } else {
            throw new IllegalArgumentException("Expression " + expr + " not supported");
        }

    }

    Comparable convertType(String operand, Class type) {
        try {
            Constructor constructor = type.getConstructor(String.class);
            Comparable o = (Comparable)constructor.newInstance(operand);
            return o;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            LOG.info("Failed to convert " + operand + " to " + type.getName());
            return operand;
        }
    }


}
