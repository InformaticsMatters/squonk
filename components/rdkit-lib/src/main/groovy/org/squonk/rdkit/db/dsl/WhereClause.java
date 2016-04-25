package org.squonk.rdkit.db.dsl;

import com.im.lac.types.MoleculeObject;
import org.squonk.rdkit.db.FingerprintType;
import org.squonk.rdkit.db.Metric;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by timbo on 13/12/2015.
 */
public class WhereClause implements IExecutable, IWherePart {

    final List<WhereClausePart> parts = new ArrayList<>();
    final Select select;

    WhereClause(Select select) {
        this.select = select;
    }

    public List<MoleculeObject> execute() {
        return select.execute();
    }

    public WhereClausePart similarityStructureQuery(String smiles, FingerprintType type, Metric metric, String outputColName) {
        return WhereClausePart.similarityStructureQuery(this, smiles, type, metric, outputColName);
    }

    public WhereClausePart substructureQuery(String smarts) {
        return WhereClausePart.substructureQuery(this, smarts);
    }

    public WhereClausePart exactStructureQuery(String smiles) {
        return WhereClausePart.exactStructureQuery(this, smiles);
    }

    public WhereClausePart equals(Column col, Object value) {
        return WhereClausePart.equals(this, col, value);
    }

    public Select select() {
        return select;
    }

    public void appendToWhereClause(StringBuilder buf, List bindVars) {
        if (parts.size() > 0) {
            buf.append("\n  WHERE ");
            for (int i = 0; i < parts.size(); i++) {
                if (i > 0) {
                    buf.append("\n    AND ");
                }
                parts.get(i).appendToWhereClause(buf, bindVars);
            }
        }
    }

}
