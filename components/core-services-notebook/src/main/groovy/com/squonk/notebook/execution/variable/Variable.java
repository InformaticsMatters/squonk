package com.squonk.notebook.execution.variable;

/**
 *
 * @author timbo
 */
public class Variable<T> {

    private final Class<T> type;
    private final String name;
    private final PersistenceType persistenceType;
    
    public enum PersistenceType {
        NONE, TEXT, JSON, BYTES, DATASET
    }

    /**
     * Constructor with persistent set to true
     *
     * @param name
     * @param type
     * @param persistenceType
     */
    public Variable(String name, Class<T> type, PersistenceType persistenceType) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            Variable v = (Variable)obj;
            return v.getName().equals(name) && v.getType() == type && v.getPersistenceType() == persistenceType;
        }
        return false;
    }
    
    

}
