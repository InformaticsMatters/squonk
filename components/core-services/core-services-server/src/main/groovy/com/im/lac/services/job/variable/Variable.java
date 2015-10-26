package com.im.lac.services.job.variable;

/**
 *
 * @author timbo
 */
public class Variable<T> {

    private final Class<T> type;
    private final String name;
    private final boolean persistent;

    /**
     * Constructor with persistent set to true
     *
     * @param name
     * @param type
     * @param persistent
     */
    protected Variable(String name, Class<T> type, boolean persistent) {
        if (name == null) {
            throw new NullPointerException("Variable name must not be null");
        }
        if (type == null) {
            throw new NullPointerException("Variable type must not be null");
        }
        this.name = name;
        this.type = type;
        this.persistent = persistent;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isPersistent() {
        return persistent;
    }

}
