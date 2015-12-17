package com.squonk.db.rdkit.dsl;

/**
 * Created by timbo on 14/12/2015.
 */
public interface IOrderByPart {
    int appendToOrderBy(StringBuilder buf);
}
