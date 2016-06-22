package org.squonk.rdkit.db.dsl;

import org.squonk.types.MoleculeObject;
import org.squonk.rdkit.db.FingerprintType;
import org.squonk.rdkit.db.Metric;
import org.squonk.rdkit.db.MolSourceType;

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

    public WhereClausePart similarityStructureQuery(String mol, MolSourceType molType, FingerprintType type, Metric metric, String outputColName) {
        return WhereClausePart.similarityStructureQuery(this, mol, molType, type, metric, outputColName);
    }

    public WhereClausePart substructureQuery(String mol, MolSourceType molType) {
        return WhereClausePart.substructureQuery(this, mol, molType);
    }

    public WhereClausePart exactStructureQuery(String mol, MolSourceType molType) {
        return WhereClausePart.exactStructureQuery(this, mol, molType);
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
