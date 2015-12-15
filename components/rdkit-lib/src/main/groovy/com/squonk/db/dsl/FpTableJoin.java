package com.squonk.db.dsl;

/**
 * Created by timbo on 14/12/2015.
 */
public class FpTableJoin {

    boolean enabled = false;
    final String joinTable;
    final Select select;

    FpTableJoin(Select select, String joinTable) {
        this.select = select;
        this.joinTable = joinTable;
    }

    void append(StringBuilder buf) {
        if (enabled) {
            buf.append("\n  JOIN ")
                    .append(joinTable)
                    .append(" m ON m.id = t.id");
        }
    }
}
