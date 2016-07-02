package org.squonk.property;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class Property<V,T> {

    private final String propertyName;
    private final String description;
    private final String metricsCode;
    private final Class<V> valueClass;
    private final Class<T> targetClass;

    public Property(String propertyName, String description, String metricsCode, Class<V> valueClass, Class<T> targetClass) {
        this.propertyName = propertyName;
        this.description = description;
        this.metricsCode = metricsCode;
        this.valueClass = valueClass;
        this.targetClass = targetClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getDescription() {
        return description;
    }

    public String getMetricsCode() {
        return metricsCode;
    }

    public Class<V> getValueClass() {
        return valueClass;
    }
}
