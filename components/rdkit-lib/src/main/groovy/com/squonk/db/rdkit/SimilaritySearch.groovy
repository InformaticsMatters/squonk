package com.squonk.db.rdkit

import groovy.transform.Canonical

/**
 * Created by timbo on 30/11/2015.
 */
@Canonical
class SimilaritySearch extends StructureSearch {
    String smiles
    double threshold
    RDKitTable.FingerprintType type
    RDKitTable.Metric metric

    SimilaritySearch() {}

    SimilaritySearch(String smiles, double threshold, RDKitTable.FingerprintType type, RDKitTable.Metric metric, int limit) {
        this.smiles = smiles
        this.threshold = threshold
        this.type = type
        this.metric = metric
        this.limit = limit
    }
}

