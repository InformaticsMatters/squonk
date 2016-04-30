package org.squonk.rdkit.db.dsl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by timbo on 14/12/2015.
 */
public class Table {

    protected final String schema;
    protected final String name;
    protected final List<Column> columns = new ArrayList<>();
    protected final String baseName;

    public Table(String name) {
        this.schema = null;
        this.name = name;
        this.baseName = name;
    }

    public Table(String schema, String name) {
        this.schema = schema;
        this.name = name;
        this.baseName = name;
    }

    protected Table(String alias, Table table) {
        this.schema = table.schema;
        this.name = alias;
        this.baseName = table.baseName;
        for (Column col : table.columns) {
            addColumn(col.name, col.type, col.definition);
        }
    }

    public boolean isAlias() {
        return name != baseName;
    }

    public Column addColumn(String name, String type, String definition) {
        Column col = new Column(this, name, type, definition);
        columns.add(col);
        return col;
    }

    public Table column(String name, String type, String definition) {
        addColumn(name, type, definition);
        return this;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public Table alias(String alias) {
        return new Table(alias, this);
    }

    public String schemaPlusTableWithAlias() {
        if (isAlias()) {
            return schemaPlusTable() + " AS " + name;
        } else {
            return schemaPlusTable();
        }
    }

    public String aliasOrSchemaPlusTable() {
        return isAlias() ? name : schemaPlusTable();
    }

    public String schemaPlusTable() {
        if (schema == null) {
            return baseName;
        } else {
            return schema + "." + baseName;
        }
    }

    public String getBaseName() {
        return baseName;
    }

    public String getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }
}
