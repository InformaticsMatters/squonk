package com.squonk.db.dsl;

import java.util.List;

/**
 * Created by timbo on 14/12/2015.
 */
public final class Column implements IProjectionPart {

    final String name;
    final String type;
    final String definition;

    final Table table;

    Column(Table table, String name, String type, String definition) {
        this.table = table;
        this.name = name;
        this.type = type;
        this.definition = definition;
    }

    public String getProjectionName() {
        return name;
    }

    public boolean isSameAs(Column col) {
        if (col != null) {
            return (this.name.equals(col.name) && this.table.getBaseTable().equals(col.table.getBaseTable()));
        }
        return false;
    }

    public int appendToProjections(StringBuilder builder, List bindVars) {
        builder.append(table.name)
                .append(".")
                .append(name);
        return 1;
    }



}
