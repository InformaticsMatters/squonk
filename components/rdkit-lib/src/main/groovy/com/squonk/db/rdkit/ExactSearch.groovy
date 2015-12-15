package com.squonk.db.rdkit

import groovy.transform.Canonical

/**
 * Created by timbo on 30/11/2015.
 */
@Canonical
class ExactSearch extends StructureSearch {
    String smiles
    boolean chiral = false

    ExactSearch() {}

    ExactSearch(String smiles, boolean chiral, int limit) {
        this.smiles = smiles
        this.chiral = chiral
        this.limit = limit
    }
}
