package com.squonk.db.rdkit

import groovy.transform.Canonical

/**
 * Created by timbo on 30/11/2015.
 */
@Canonical
class SubstructureSearch extends StructureSearch {
    String smarts
    boolean chiral = false

    SubstructureSearch() {}

    SubstructureSearch(String smarts, boolean chiral, int limit) {
        this.smarts = smarts
        this.chiral = chiral
        this.limit = limit
    }
}
