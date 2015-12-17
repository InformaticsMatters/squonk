package com.squonk.db.rdkit.dsl;

import com.im.lac.types.MoleculeObject;

import java.util.List;

/**
 * Created by timbo on 13/12/2015.
 */
public class LimitClause implements IExecutable {

    final Select select;
    final int limit;

    LimitClause(Select select, int limit) {
        this.select = select;
        this.limit = limit;
    }

    void append(StringBuilder buf) {
        if (limit > 0) {
            buf.append("\n  LIMIT ").append(limit);
        }
    }

    @Override
    public List<MoleculeObject> execute() {
        return select.execute();
    }

}
