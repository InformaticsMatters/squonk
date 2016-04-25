package org.squonk.rdkit.db.dsl;

import org.squonk.rdkit.db.FingerprintType;
import org.squonk.rdkit.db.Metric;

import java.util.List;

/**
 * Created by timbo on 14/12/2015.
 */
public interface IWherePart {

    WhereClausePart similarityStructureQuery(String smiles, FingerprintType type, Metric metric, String outputColName);

    WhereClausePart substructureQuery(String smarts);

    WhereClausePart exactStructureQuery(String smiles);

    WhereClausePart equals(Column col, Object value);

    Select select();

    void appendToWhereClause(StringBuilder buf, List bindVars);
}
