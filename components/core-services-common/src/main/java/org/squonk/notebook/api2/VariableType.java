package org.squonk.notebook.api2;

public enum VariableType {
    STRING,
    INTEGER,
    FLOAT,
    DATASET,
    FILE,
    STREAM;
    private final static long serialVersionUID = 1l;
    private VariableType() {
    }
}
