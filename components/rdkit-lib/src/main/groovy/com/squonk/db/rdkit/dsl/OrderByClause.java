package com.squonk.db.rdkit.dsl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by timbo on 14/12/2015.
 */
public class OrderByClause {

    final Select select;
    final List<IOrderByPart> parts = new ArrayList<>();

    OrderByClause(Select select) {
        this.select = select;
    }

    void add(Column col, boolean ascending) {
        add(new OrderByPart(this, col, ascending));
    }

    void add(IOrderByPart orderBy) {
        parts.add(orderBy);
    }

    void appendToOrderBy(StringBuilder buf) {
        int count = 0;
        for (IOrderByPart part : parts) {
            if (count == 0) {
                buf.append("\n  ORDER BY ");
            } else {
                buf.append(",");
            }
            count += part.appendToOrderBy(buf);
        }

    }
}
