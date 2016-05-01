package org.squonk.rdkit.db.dsl;

import org.squonk.rdkit.db.FingerprintType;
import org.squonk.rdkit.db.Metric;
import org.squonk.rdkit.db.MolSourceType;

import java.util.List;

/**
 * Created by timbo on 14/12/2015.
 */
public interface IWherePart {

    WhereClausePart similarityStructureQuery(String mol, MolSourceType molType, FingerprintType type, Metric metric, String outputColName);

    WhereClausePart substructureQuery(String mol, MolSourceType molType);

    WhereClausePart exactStructureQuery(String mol, MolSourceType molType);

    WhereClausePart equals(Column col, Object value);

    Select select();

    void appendToWhereClause(StringBuilder buf, List bindVars);
}
