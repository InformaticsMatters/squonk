package org.squonk.rdkit.db.dsl;

/**
 * Created by timbo on 14/12/2015.
 */
public class FpTableJoin {

    boolean enabled = false;
    final String joinScheamPlusTable;
    final Select select;

    FpTableJoin(Select select, String joinScheamPlusTable) {
        this.select = select;
        this.joinScheamPlusTable = joinScheamPlusTable;
    }

    void append(StringBuilder buf) {
        if (enabled) {
            buf.append("\n  JOIN ")
                    .append(joinScheamPlusTable)
                    .append(" m ON m.id = t.id");
        }
    }
}
