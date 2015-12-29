package org.squonk.db.rdkit.dsl;

import org.squonk.db.rdkit.FingerprintType;
import org.squonk.db.rdkit.Metric;

import java.util.List;

/**
 * Created by timbo on 14/12/2015.
 */
public interface IWherePart {

    WhereClausePart similarityStructureQuery(String smiles, FingerprintType type, Metric metric, String outputColName);

    WhereClausePart substructureQuery(String smarts);

    WhereClausePart exactStructureQuery(String smiles);

    WhereClausePart equals(Column col, Object value);

    void appendToWhereClause(StringBuilder buf, List bindVars);
}