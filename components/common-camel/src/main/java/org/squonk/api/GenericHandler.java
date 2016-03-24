package org.squonk.api;

import org.apache.camel.TypeConverter;
import org.apache.camel.spi.TypeConverterRegistry;

/**
 * Created by timbo on 23/03/2016.
 */
public interface GenericHandler<P,G> {

    void setGenericType(Class<G> genericType);

     default boolean canConvertGeneric(Class<? extends Object> otherGenericType, TypeConverterRegistry registry) {
         return false;
     }

    default P convertGeneric(P from, Class<? extends Object> otherGenericType, TypeConverterRegistry registry) {
        throw new RuntimeException("There is no default way to handle generic conversions. Implementations must handle this.");
    }
}
