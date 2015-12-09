package com.squonk.db

/**
 * Created by timbo on 08/12/2015.
 */
class DbQuery {

    List<DbColumn> select = []
    List<DbTable> from = []
    List<WhereClause> where = []

    DbQuery select(DbColumn... cols) {
        select.addAll(cols as List)

        return this;
    }

    DbQuery from(DbTable table) {
        from << table
        return this
    }

    DbQuery whereEquals(DbColumn col, Object value) {
        where << new EqualsValueWhereClause(col, value)
        return this
    }

    String toSql() {
        StringBuilder b = new StringBuilder("SELECT ")

        if (select) {
            b.append select*.name.join("'")
        } else {
            b.append "*"
        }
        if (from) {
            b.append " FROM "
            b.append from.collect {
                "${it.schema}.${it.name}"
            }.join(",")
        } else {
            throw new IllegalStateException("No table to select from")
        }
        if (where) {
            b.append(" WHERE ")

        }
    }

    interface WhereClause {

    }

    class EqualsValueWhereClause implements WhereClause {
        DbColumn col
        Object value

        EqualsValueWhereClause(DbColumn col, Object value) {
            this.col = col
            this.value = value
        }
    }
}
