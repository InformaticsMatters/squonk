package com.im.lac.services.job.variable;

/**
 *
 * @author timbo
 */
public class Variable<T> {

    private final Class<T> type;
    private final String name;
    private final PersistenceType persistenceType;
    
    public enum PersistenceType {
        NONE, TEXT, JSON, BYTES
    }

    /**
     * Constructor with persistent set to true
     *
     * @param name
     * @param type
     * @param persistenceType
     */
    protected Variable(String name, Class<T> type, PersistenceType persistenceType) {
        if (name == null) {
            throw new NullPointerException("Variable name must not be null");
        }
        if (type == null) {
            throw new NullPointerException("Variable type must not be null");
        }
        if (persistenceType == null) {
            throw new NullPointerException("Variable persistence type must not be null");
        }
        this.name = name;
        this.type = type;
        this.persistenceType = persistenceType;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public PersistenceType getPersistenceType() {
        return persistenceType;
    }

}
