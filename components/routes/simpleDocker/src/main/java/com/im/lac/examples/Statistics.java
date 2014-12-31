package com.im.lac.examples;

import java.util.List;

/**
 *
 * @author timbo
 */
public class Statistics {
    private final List<Number> values;
    private final Float sum;
    private final int count;    
    
    Statistics(List<Number> values, Float sum, Integer count) {
        this.values = values;
        this.sum = sum;
        this.count = count;
    }

    public List<Number> getValues() {
        return values;
    }

    public Float getSum() {
        return sum;
    }

    public int getCount() {
        return count;
    }

}
