package com.squonk.db

import groovy.transform.Canonical

/**
 * Created by timbo on 08/12/2015.
 */
@Canonical
class DbTable {

    final String schema
    final String name

    private final List<DbColumn> columns = []
    private List<DbColumn> pkColumns = []


    List<DbColumn> getColumns() {
        return Collections.unmodifiableList(columns)
    }

    DbColumn addColumn(String name, String definition) {
        DbColumn col = new DbColumn(this, name, definition)
        columns << col
        return col
    }

    DbTable withColumn(String name, String definition) {
        addColumn(name, definition)
        return this
    }

    void setPrimaryKey(DbColumn... cols) {
        pkColumns = cols as List
    }

    DbColumn findColumn(String name) {
        return columns.find {it.name.equalsIgnoreCase(name)}
    }

    void select(DbColumn col, Object value) {
        "SELECT * from $schema.$name WHERE ${col.name} = ?"
    }

    String buildCreateTableSql() {
        StringBuilder b = new StringBuilder()
        b.append("CREATE TABLE $schema.$name (")
        columns.eachWithIndex { col, i ->
            if (i > 0) b.append(',')
            b.append("\n  $col.name $col.definition")
        }
        if (pkColumns) {
            b.append ",\n  PRIMARY KEY ("
            b.append pkColumns*.name.join(",")
            b.append(")")
        }
        b.append("\n)")
        return b.toString()
    }

}
