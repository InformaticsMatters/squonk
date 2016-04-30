package org.squonk.rdkit.db.dsl;

import org.squonk.rdkit.db.RDKitTable;

/**
 * Created by timbo on 14/12/2015.
 */
public class FpTableJoin {

    boolean enabled = false;
    final Select select;

    FpTableJoin(Select select) {
        this.select = select;
    }

    void append(StringBuilder buf) {
        if (enabled) {
            RDKitTable rdk = select.query.rdkTable;
            buf.append("\n  JOIN ")
                    .append(rdk.getMolFpTable().schemaPlusTableWithAlias())
                    .append(" ON " + rdk.getMolFpTable().aliasOrSchemaPlusTable() + ".id = " + rdk.aliasOrSchemaPlusTable() + ".id");
        }
    }
}
