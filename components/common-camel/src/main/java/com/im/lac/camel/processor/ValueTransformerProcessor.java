package com.im.lac.camel.processor;

import com.im.lac.types.BasicObject;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.transform.AbstractTransform;
import com.squonk.dataset.transform.ConvertFieldTransform;
import com.squonk.dataset.transform.DeleteFieldTransform;
import com.squonk.dataset.transform.RenameFieldTransform;
import com.squonk.dataset.transform.TransformDefinitions;
import java.io.IOException;
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
                vtp.convertValueType(cf.getFieldName(), cf.getNewType());
            }
        }
        return vtp;
    }

    public ValueTransformerProcessor convertValueType(String fldName, Class newClass) {
        conversions.add(new TypeConversion(fldName, newClass));
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

    class TypeConversion implements Conversion {

        final String fldName;
        final Class newClass;

        TypeConversion(String fldName, Class newClass) {
            this.fldName = fldName;
            this.newClass = newClass;
        }

        @Override
        public Stream<BasicObject> execute(TypeConverter converter, Stream<BasicObject> stream) {

            return stream.peek((o) -> {
                Object old = o.getValue(fldName);
                if (old != null) {
                    Object neu = converter.convertTo(newClass, old);
                    o.putValue(fldName, neu);
                }
            });
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
