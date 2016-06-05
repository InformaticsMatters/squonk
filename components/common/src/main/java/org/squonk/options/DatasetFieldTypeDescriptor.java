package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Defines a type of option for a Dataset field (value) allowing to filter based on criteria such as the value type (class).
 *
 * Created by timbo on 03/02/16.
 */
public class DatasetFieldTypeDescriptor extends SimpleTypeDescriptor<String> {

    private final Class[] typeFilters;

    public DatasetFieldTypeDescriptor(@JsonProperty("typeFilters") Class[] typeFilters) {
        super(String.class);
        this.typeFilters = typeFilters;
    }

    public DatasetFieldTypeDescriptor() {
        this(new Class[0]);
    }

    public Class[] getTypeFilters() {
        return typeFilters;
    }


    public boolean filter(String name, Class type) {
        if (typeFilters == null || typeFilters.length == 0) {
            return true;
        }
        for (Class cls : typeFilters) {
            if (cls.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }


}
