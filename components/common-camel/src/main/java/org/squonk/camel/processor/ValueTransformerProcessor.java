package org.squonk.camel.processor;

import groovy.lang.GroovyClassLoader;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConverter;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.transform.*;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Processor that handles transforming values of{@link BasicObject}s. Follows
 * the fluent builder pattern.
 *
 * @author timbo
 */
public class ValueTransformerProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(ValueTransformerProcessor.class.getName());

    private int classCount = 0;

    private final List<Conversion> conversions = new ArrayList<>();
    private GroovyClassLoader groovyClassLoader;
    private String errorFieldName = "TransformErrors";

    private GroovyClassLoader getGroovyClassLoader() {
        if (groovyClassLoader == null) {
            groovyClassLoader = new GroovyClassLoader();
        }
        return groovyClassLoader;
    }

    @Override
    public void process(Exchange exch) throws Exception {
        TypeConverter typeConverter = exch.getContext().getTypeConverter();
        Dataset ds = exch.getIn().getBody(Dataset.class);
        Dataset neu = execute(typeConverter, ds);
        exch.getIn().setBody(neu);
    }

    /**
     * Add operations to perform the conversions, updating the dataset with the
     * transformed stream, and adding appropriate field metadata. NOTE: this does NOT perform a terminal operation on
     * the stream, so after calling this method the values are not yet transformed. The resulting stream must be processed
     * by performing a terminal operation.
     *
     * @param typeConverter
     * @param dataset
     * @throws IOException
     */
    public Dataset<? extends BasicObject> execute(TypeConverter typeConverter, Dataset<BasicObject> dataset) throws IOException {
        Stream<BasicObject> stream = addConversions(typeConverter, dataset);

        Class type = dataset.getType();
        for (Conversion conversion : conversions) {
            type = conversion.getObjectType(type);
        }
        DatasetMetadata oldMeta = dataset.getMetadata();
        DatasetMetadata newMeta = new DatasetMetadata(type, null, oldMeta == null ? null : oldMeta.getFieldMetaProps(), -1, oldMeta == null ? null : oldMeta.getProperties());
        Dataset newData = new Dataset(type, stream, newMeta);

        return newData;
    }

    public Stream<BasicObject> addConversions(TypeConverter typeConverter, Dataset<BasicObject> dataset) throws IOException {
        Stream<BasicObject> stream = dataset.getStream();
        for (Conversion conversion : conversions) {
            LOG.info("Handling conversion: " + conversion);
            stream = conversion.execute(typeConverter, stream);
            if (dataset.getMetadata() != null) {
                conversion.updateMetadata(dataset.getMetadata());
            }
        }
        return stream;
    }

    public static ValueTransformerProcessor create(TransformDefinitions txdefs) {

        ValueTransformerProcessor vtp = new ValueTransformerProcessor();
        for (AbstractTransform tx : txdefs.getTransforms()) {
            if (tx instanceof DeleteRowTransform) {
                DeleteRowTransform df = (DeleteRowTransform) tx;
                vtp.deleteRow(df.getCondition());
            } else if (tx instanceof DeleteFieldTransform) {
                DeleteFieldTransform df = (DeleteFieldTransform) tx;
                vtp.deleteValue(df.getFieldName(), df.getCondition());
            } else if (tx instanceof RenameFieldTransform) {
                RenameFieldTransform rf = (RenameFieldTransform) tx;
                vtp.convertValueName(rf.getFieldName(), rf.getNewName());
            } else if (tx instanceof ConvertFieldTransform) {
                ConvertFieldTransform cf = (ConvertFieldTransform) tx;
                vtp.convertValueType(cf.getFieldName(), cf.getNewType(), cf.getGenericType(), cf.getOnError());
            } else if (tx instanceof ReplaceValueTransform) {
                ReplaceValueTransform cf = (ReplaceValueTransform) tx;
                vtp.replaceValue(cf.getFieldName(), cf.getMatch(), cf.getResult());
            } else if (tx instanceof ConvertToMoleculeTransform) {
                ConvertToMoleculeTransform cf = (ConvertToMoleculeTransform) tx;
                vtp.convertToMolecule(cf.getStructureFieldName(), cf.getStructureFormat());
            } else if (tx instanceof AssignValueTransform) {
                AssignValueTransform cf = (AssignValueTransform) tx;
                vtp.assignValue(cf.getFieldName(), cf.getExpression(), cf.getCondition(), cf.getOnError());
            }
        }
        return vtp;
    }


    public ValueTransformerProcessor replaceValue(String fldName, Object match, Object result) {
        conversions.add(new ReplaceValueConversion(fldName, match, result));
        return this;
    }

    public ValueTransformerProcessor assignValue(String fldName, String expression, String condition, String onError) {
        conversions.add(new AssignConversion(fldName, expression, condition, onError));
        return this;
    }

    public ValueTransformerProcessor convertValueType(String fldName, Class newClass) {
        return convertValueType(fldName, newClass, null, null);
    }

    public ValueTransformerProcessor convertValueType(String fldName, Class newClass, Class genericType, String onError) {

        if (genericType != null) {
            // check that its got the right constructor to save nasty exceptions later
            try {
                Constructor c = newClass.getConstructor(Object.class, Class.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Illegal conversion. Must have constructor that takes Object, Class");
            }
        }

        conversions.add(new TypeConversion(fldName, newClass, genericType, onError));
        return this;
    }

    public ValueTransformerProcessor convertValueName(String oldName, String newName) {
        conversions.add(new RenameFieldConversion(oldName, newName));
        return this;
    }

    public ValueTransformerProcessor deleteValue(String name, String condition) {
        conversions.add(new DeleteFieldConversion(name, condition));
        return this;
    }

    public ValueTransformerProcessor deleteRow(String condition) {
        conversions.add(new DeleteRowConversion(condition));
        return this;
    }

    public ValueTransformerProcessor convertToMolecule(String structureField, String sturctureFormat) {
        conversions.add(new ConvertToMoleculeConversion(structureField, sturctureFormat));
        return this;
    }


    private abstract class Conversion {

        abstract Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream);

        abstract void updateMetadata(DatasetMetadata meta);

        Class getObjectType(Class input) {
            return input;
        }

    }

    class ReplaceValueConversion extends Conversion {

        final String fldName;
        final Object match;
        final Object result;

        ReplaceValueConversion(String fldName, Object match, Object result) {
            this.fldName = fldName;
            this.match = match;
            this.result = result;
        }

        void updateMetadata(DatasetMetadata meta) {
            meta.appendFieldHistory(fldName, "Value conversion: " + match.toString() + " -> " + result.toString());
        }

        @Override
        Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {
            return stream.peek((o) -> {
                Object old = o.getValue(fldName);
                if (old == null) {
                    if (match == null) {
                        // null matches null
                        o.putValue(fldName, result);
                    }
                } else {
                    if (old.equals(match)) {
                        // old matches match
                        o.putValue(fldName, result);
                    }
                }
            });
        }

        @Override
        public String toString() {
            return "ReplaceValueConversion [fldName:" + fldName + " match: " + match + " result: " + result + "]";
        }

    }

    class TypeConversion extends Conversion {

        final String fldName;
        final Class newClass;
        final Class genericClass;
        final String onError;

        TypeConversion(String fldName, Class newClass, Class genericClass, String onError) {
            validateOnError(onError);

            this.fldName = fldName;
            this.newClass = newClass;
            this.genericClass = genericClass;
            this.onError = onError;
        }

        @Override
        void updateMetadata(DatasetMetadata meta) {
            if (genericClass == null) {
                meta.appendFieldHistory(fldName, "Type conversion to " + newClass.getName());
            } else {
                meta.appendFieldHistory(fldName, "Type conversion to " + newClass.getName() + "<" + genericClass.getName() + ">");
            }
        }

        @Override
        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {

            return stream.peek((o) -> {

                Object old = o.getValue(fldName);
                if (old != null) {
                    try {
                        Object neu = null;
                        if (genericClass != null) {
                            neu = genericCreate(old, newClass, genericClass);
                        } else {
                            neu = converter.convertTo(newClass, old);
                        }
                        o.putValue(fldName, neu);
                    } catch (Exception e) {
                        if (onError == null || "fail".equals(onError)) {
                            throw e;
                        } else if ("continue".equals(onError)) {
                            o.getValues().remove(fldName);
                            String current = o.getValue(errorFieldName, String.class);
                            String msg = "Failed to convert " + old + " for field " + fldName + " to " + newClass.getSimpleName();
                            if (current == null) {
                                o.putValue(errorFieldName, msg);
                            } else {
                                o.putValue(errorFieldName, current + "\n" + msg);
                            }
                        }
                    }
                }
            });
        }

        private Object genericCreate(Object value, Class type, Class genericType) {
            try {
                Constructor c = type.getConstructor(Object.class, Class.class);
                return c.newInstance(value, genericType);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Failed to instantiate value", e);
            }
        }

        @Override
        public String toString() {
            return "TypeConversion [fldName:" + fldName + " newClass: " + newClass + " genericClass: " + genericClass + " onError: " + onError + "]";
        }
    }

    class RenameFieldConversion extends Conversion {

        final String oldName;
        final String newName;

        RenameFieldConversion(String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
        }

        @Override
        void updateMetadata(DatasetMetadata meta) {
            String old = (String) meta.getFieldMetaProp(oldName, DatasetMetadata.PROP_HISTORY);
            if (old != null) {
                meta.appendFieldHistory(newName, old);
            }
            meta.appendFieldHistory(newName, "Renamed from  " + oldName + " to " + newName);
        }

        @Override
        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {

            return stream.peek((o) -> {
                Object value = o.getValue(oldName);
                if (value != null) {
                    o.putValue(newName, value);
                    o.getValues().remove(oldName);
                }
            });
        }

        @Override
        public String toString() {
            return "RenameFieldConversion [oldName:" + oldName + " newName: " + newName + "]";
        }
    }

    class DeleteFieldConversion extends Conversion {

        final String fldName, condition;

        DeleteFieldConversion(String fldName, String condition) {
            this.fldName = fldName;
            this.condition = condition;
        }

        @Override
        void updateMetadata(DatasetMetadata meta) {
            if (condition != null) {
                meta.appendFieldHistory(fldName, "Deleted values matching condition: " + condition);
            } else {
                meta.appendDatasetHistory("Deleted field " + fldName);
            }
        }

        @Override
        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {

            if (condition == null) {
                return stream.peek((o) -> {
                    o.getValues().remove(fldName);
                });
            } else {
                try {
                    final Predicate predicate = createPredicteClass(condition);
                    return stream.peek((o) -> {
                        if (predicate.test(o)) {
                            o.getValues().remove(fldName);
                        }
                    });
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new IllegalStateException("Failed to create filter", e);
                }
            }
        }

        @Override
        public String toString() {
            return "DeleteFieldConversion [fldName:" + fldName + " condition: " + condition + "]";
        }
    }

    class AssignConversion extends Conversion {

        final String fldName;
        final String expression;
        final String condition;
        final String onError;

        AssignConversion(String fldName, String expression, String condition, String onError) {
            validateOnError(onError);
            this.fldName = fldName;
            this.expression = expression;
            this.condition = condition;
            this.onError = onError;
        }

        @Override
        void updateMetadata(DatasetMetadata meta) {
            if (meta.getValueClassMappings().get(fldName) == null) {
                meta.putFieldMetaProp(fldName, DatasetMetadata.PROP_CREATED, meta.now());
                meta.putFieldMetaProp(fldName, DatasetMetadata.PROP_SOURCE, "Squonk assignment potion");
            }
            if (condition == null) {
                meta.appendFieldHistory(fldName, "Assignment: " + expression);
            } else {
                meta.appendFieldHistory(fldName, "Assignment: " + expression + " IF " + condition);
            }
        }

        @Override
        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {

            Consumer c;
            try {
                c = createConsumer();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new IllegalStateException("Failed to create consumer", e);
            }

            return stream.peek(c);
        }

        private Consumer createConsumer() throws IllegalAccessException, InstantiationException {

            String clsDef = createConsumerClassDefinition();
            LOG.info("Built Consumer class:\n" + clsDef);
            Class<Consumer> cls = getGroovyClassLoader().parseClass(clsDef);
            Consumer consumer = cls.newInstance();
            return consumer;
        }


        private String createConsumerClassDefinition() {
            StringBuilder b1 = new StringBuilder();
            b1.append("import static java.lang.Math.*\n")
                    .append("class MyConsumer").append(++classCount).append(" implements java.util.function.Consumer {\n")
                    .append("  void accept(def o) {\n")
                    .append("    o.values.with { ");

            if (condition != null) {
                b1.append("if ( ").append(condition).append(" ) { ");
            }

            StringBuilder b2 = new StringBuilder();
            b2.append(fldName).append(" = ").append(expression);

            if ("continue".equals(onError)) {
                b1.append(wrapErrorHandler(b2.toString(), "Failed to evaluate field " + fldName));
            } else if (onError == null || "fail".equals(onError)) {
                b1.append(b2.toString());
            }

            if (condition != null) {
                b1.append("\n    }");
            }

            b1.append("\n    }\n  }\n}");
            return b1.toString();
        }

        @Override
        public String toString() {
            return "AssignConversion [fldName:" + fldName + " expression: " + expression + " condition: " + condition + " orError: " + onError + "]";
        }
    }


    class ConvertToMoleculeConversion extends Conversion {

        final String field, format;

        ConvertToMoleculeConversion(String structureField, String structureFormat) {
            this.field = structureField;
            this.format = structureFormat;
        }

        @Override
        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {

            if (field == null) {
                throw new NullPointerException("Field containing structure must be specified");
            }
            if (format == null) {
                throw new NullPointerException("Structure format must be specified");
            }

            return stream.map((o) -> {
                Object mol = o.getValue(field);
                MoleculeObject mo = new MoleculeObject(o.getUUID(), mol == null ? null : mol.toString(), format, o.getValues());
                mo.getValues().remove(field);
                return mo;
            });
        }

        @Override
        void updateMetadata(DatasetMetadata meta) {
            meta.appendDatasetHistory("Converted to molecules using field " + field + " and format " + format);
        }

        @Override
        Class getObjectType(Class input) {
            return MoleculeObject.class;
        }

        @Override
        public String toString() {
            return "ConvertToMoleculeConversion [field:" + field + " format: " + format + "]";
        }
    }

    class DeleteRowConversion extends Conversion {

        final String condition;

        DeleteRowConversion(String condition) {
            this.condition = condition;
        }

        @Override
        void updateMetadata(DatasetMetadata meta) {
            if (condition != null) {
                meta.appendDatasetHistory("Deleted rows where: " + condition);
            } else {
                meta.appendDatasetHistory("Deleted all rows");
            }
        }

        @Override
        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {
            Predicate p;
            try {
                p = createPredicate();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new IllegalStateException("Failed to create filter", e);
            }

            return stream.filter(p);
        }

        private Predicate createPredicate() throws IllegalAccessException, InstantiationException {
            if (condition == null) {
                return o -> false;
            } else {
                Predicate predicate = createPredicteClass("!(" + condition + ")");
                return predicate;
            }
        }

        @Override
        public String toString() {
            return "DeleteRowConversion [condition:" + condition + "]";
        }
    }

    private Predicate createPredicteClass(String condition)
            throws IllegalAccessException, InstantiationException {
        String clsdef = createPredicateClassDefinition(condition);
        LOG.info("Predicate class: \n" + clsdef);
        Class<Predicate> cls = getGroovyClassLoader().parseClass(clsdef);
        Predicate predicate = cls.newInstance();
        return predicate;
    }

    private String createPredicateClassDefinition(String condition) {
        StringBuilder b = new StringBuilder()
                .append("import static java.lang.Math.*\n")
                .append("class MyPredicate").append(++classCount).append(" implements java.util.function.Predicate {\n")
                .append("  boolean test(def o) {\n")
                .append("    o.values.with { ")
                .append(condition)
                .append(" }\n  }\n}");
        return b.toString();
    }

    private String wrapErrorHandler(String expr, String message) {
        StringBuilder b = new StringBuilder("\n      try { ")
                .append(expr)
                .append(" } catch (Exception e) { \n")
                .append(errorFieldName)
                .append(" = ( ")
                .append(errorFieldName)
                .append(" == null ? '")
                .append(message)
                .append(". ' + e.class.simpleName + '[' + e.message")
                .append("+ ']' : ")
                .append(errorFieldName).append(" + '\\n' + '").append(message)
                .append(". ' + e.class.simpleName + '[' + e.message")
                .append(" + ']' )\n}");

        return b.toString();
    }

    /**
     * @param onError
     * @throws IllegalArgumentException if not fail or continue
     */
    void validateOnError(String onError) {
        if (onError != null) {
            if ("fail".equals(onError) || "continue".equals(onError)) {
                return;
            }
            throw new IllegalArgumentException("Bad onError value: " + onError);
        }
    }

}
