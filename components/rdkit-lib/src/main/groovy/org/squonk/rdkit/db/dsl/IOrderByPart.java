package org.squonk.rdkit.db.dsl;

/**
 * Created by timbo on 14/12/2015.
 */
public interface IOrderByPart {
    int appendToOrderBy(StringBuilder buf);
}
