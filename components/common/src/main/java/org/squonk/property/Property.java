package org.squonk.property;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class Property<V,T> {

    private final String propertyName;
    private final String description;
    private final Class<V> valueClass;
    private final Class<T> targetClass;

    public Property(String propertyName, String description, Class<V> valueClass, Class<T> targetClass) {
        this.propertyName = propertyName;
        this.description = description;
        this.valueClass = valueClass;
        this.targetClass = targetClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getDescription() {
        return description;
    }

    public Class<V> getValueClass() {
        return valueClass;
    }
}
