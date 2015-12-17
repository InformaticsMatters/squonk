package com.squonk.db.rdkit.dsl;

/**
 * Created by timbo on 13/12/2015.
 */
public class SimpleStatement {

    private final String cmd;

    SimpleStatement(String cmd) {
        this.cmd = cmd;
    }

    public String getCommand() {
        return cmd;
    }
}
