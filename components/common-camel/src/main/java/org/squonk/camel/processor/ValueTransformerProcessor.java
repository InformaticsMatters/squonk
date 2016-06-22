package org.squonk.camel.processor;

import org.squonk.types.BasicObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.transform.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConverter;

/**
 * Processor that handles transforming values of{@link BasicObject}s. Follows
 * the fluent builder pattern.
 *
 * @author timbo
 */
public class ValueTransformerProcessor implements Processor {

    private final List<Conversion> conversions = new ArrayList<>();

    @Override
    public void process(Exchange exch) throws Exception {
        TypeConverter typeConverter = exch.getContext().getTypeConverter();
        Dataset ds = exch.getIn().getBody(Dataset.class);
        execute(typeConverter, ds);
    }

    /**
     * Add operations to perform the conversions, updating the dataset with the
     * transformed stream. NOTE: this does NOT perform a terminal operation on
     * the stream, so after calling this method the values are not yet
     * transformed. The resulting stream must be processed by performing a
     * terminal operation
     *
     * @param typeConverter
     * @param dataset
     * @throws IOException
     */
    public void execute(TypeConverter typeConverter, Dataset dataset) throws IOException {
        Stream<BasicObject> stream = addConversions(typeConverter, dataset.getStream());
        dataset.replaceStream(stream);
    }

    public Stream<BasicObject> addConversions(TypeConverter typeConverter, Stream<BasicObject> stream) throws IOException {
        for (Conversion conversion : conversions) {
            stream = conversion.execute(typeConverter, stream);
        }
        return stream;
    }

    public static ValueTransformerProcessor create(TransformDefinitions txdefs) {

        ValueTransformerProcessor vtp = new ValueTransformerProcessor();
        for (AbstractTransform tx : txdefs.getTransforms()) {
            if (tx instanceof DeleteFieldTransform) {
                DeleteFieldTransform df = (DeleteFieldTransform) tx;
                vtp.deleteValue(df.getFieldName());
            } else if (tx instanceof RenameFieldTransform) {
                RenameFieldTransform rf = (RenameFieldTransform) tx;
                vtp.convertValueName(rf.getFieldName(), rf.getNewName());
            } else if (tx instanceof ConvertFieldTransform) {
                ConvertFieldTransform cf = (ConvertFieldTransform) tx;
                vtp.convertValueType(cf.getFieldName(), cf.getNewType(), cf.getGenericType());
            } else if (tx instanceof TransformValueTransform) {
                TransformValueTransform cf = (TransformValueTransform) tx;
                vtp.transformValue(cf.getFieldName(), cf.getMatch(), cf.getResult());
            }
        }
        return vtp;
    }


    public ValueTransformerProcessor transformValue(String fldName, Object match, Object result) {
        conversions.add(new TransformConversion(fldName, match, result));
        return this;
    }

    public ValueTransformerProcessor convertValueType(String fldName, Class newClass) {
        return convertValueType(fldName, newClass, null);
    }

    public ValueTransformerProcessor convertValueType(String fldName, Class newClass, Class genericType) {

        if (genericType != null) {
            // check that its got the right constructor to save nasty exceptions later
            try {
                Constructor c = newClass.getConstructor(Object.class, Class.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Illegal conversion. Must have constructor that takes Object, Class");
            }
        }

        conversions.add(new TypeConversion(fldName, newClass, genericType));
        return this;
    }

    public ValueTransformerProcessor convertValueName(String oldName, String newName) {
        conversions.add(new NameConversion(oldName, newName));
        return this;
    }

    public ValueTransformerProcessor deleteValue(String name) {
        conversions.add(new DeleteConversion(name));
        return this;
    }

    interface Conversion {

        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream);
    }

    class TransformConversion implements Conversion {

        final String fldName;
        final Object match;
        final Object result;

        TransformConversion(String fldName, Object match, Object result)  {
            this.fldName = fldName;
            this.match = match;
            this.result = result;
        }

        @Override
        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {
            return stream.peek((o) -> {
                Object old = o.getValue(fldName);
                if (old == null) {
                    if (match == null)  {
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
    }

    class TypeConversion implements Conversion {

        final String fldName;
        final Class newClass;
        final Class genericClass;

        TypeConversion(String fldName, Class newClass, Class genericClass) {
            this.fldName = fldName;
            this.newClass = newClass;
            this.genericClass = genericClass;
        }

        @Override
        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {

            return stream.peek((o) -> {
                Object old = o.getValue(fldName);
                if (old != null) {
                    Object neu = null;
                    if (genericClass != null) {
                        neu = genericCreate(old, newClass, genericClass);
                    } else {
                        neu = converter.convertTo(newClass, old);
                    }
                    o.putValue(fldName, neu);
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
    }

    class NameConversion implements Conversion {

        final String oldName;
        final String newName;

        NameConversion(String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
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
    }

    class DeleteConversion implements Conversion {

        final String fldName;

        DeleteConversion(String fldName) {
            this.fldName = fldName;
        }

        @Override
        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {

            return stream.peek((o) -> {
                o.getValues().remove(fldName);
            });
        }
    }

}
