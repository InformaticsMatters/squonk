package com.squonk.db.dsl;

/**
 * Created by timbo on 14/12/2015.
 */
public class OrderByPart implements IOrderByPart {

    final OrderByClause orderByClause;
    final Column col;
    final boolean ascending;


    OrderByPart(OrderByClause orderByClause, Column col, boolean ascending) {
        this.orderByClause = orderByClause;
        this.col = col;
        this.ascending = ascending;
    }

    public int appendToOrderBy(StringBuilder buf) {
        buf.append(col.table.name).append(".").append(col.name).append(" ").append(ascending ? "ASC" : "DESC");
        return 1;
    }
}
