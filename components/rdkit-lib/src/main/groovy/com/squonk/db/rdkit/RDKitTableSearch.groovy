package com.squonk.db.rdkit

import com.im.lac.types.MoleculeObject
import groovy.sql.Sql
import groovy.util.logging.Log

import javax.sql.DataSource

/**
 *
 * @author timbo
 */
@Log
class RDKitTableSearch extends RDKitTable {

    RDKitTableSearch(DataSource dataSource, String schema, String baseTable, MolSourceType molSourceType, Map<String, String> extraColumnDefs) {
        super(dataSource, schema, baseTable, molSourceType, extraColumnDefs)
    }

    List<MoleculeObject> search(StructureSearch search) {
        if (search.getClass() == SimilaritySearch.class) {
            return similaritySearch((SimilaritySearch) search);
        } else if (search.getClass() == SubstructureSearch.class) {
            return substructureSearch((SubstructureSearch) search);
        } else if (search.getClass() == ExactSearch.class) {
            return exactSearch((ExactSearch) search);
        }
    }

    List<MoleculeObject> similaritySearch(SimilaritySearch search) {
        return similaritySearch(search.smiles, search.threshold, search.type, search.metric, search.limit)
    }

    List<MoleculeObject> similaritySearch(String smiles, double threshold, FingerprintType type, Metric metric) {
        return similaritySearch(smiles, threshold, type, metric, null)
    }

    List<MoleculeObject> similaritySearch(String smiles, double threshold, FingerprintType type, Metric metric, Integer limit) {

        String fn = getSimSearchHelperFunctionName(type, metric)
        String sql1 = 'SET ' + metric.simThresholdProp + ' = ' + threshold
        String sql2 = 'SELECT * FROM ' + fn + '(?)' + (limit == null || limit < 1 ? '' : ' LIMIT ' + limit)

        log.fine "SQL: $sql1"
        log.fine "SQL: $sql2"
        return executeSql { db ->
            db.execute(sql1)
            return executeBuildMols(db, sql2, [smiles])
        }
    }

    List<MoleculeObject> substructureSearch(SubstructureSearch search) {
        return substructureSearch(search.smarts, search.chiral, search.limit)
    }


    List<MoleculeObject> substructureSearch(String smarts, boolean chiral) {
        substructureSearch(smarts, chiral, null)
    }

    List<MoleculeObject> substructureSearch(String smarts, boolean chiral, Integer limit) {
        String sql1 = 'SET rdkit.do_chiral_sss=' + chiral
        String sql2 = 'SELECT b.*, m.m FROM ' + baseSchemaPlusTable() + ' b JOIN ' +
                molfpsSchemaPlusTable() + ' m ON b.id = m.id WHERE m.m @> ?::qmol' +
                (limit == null || limit < 1 ? '' : ' LIMIT ' + limit)
        log.fine "SQL: $sql1"
        log.fine "SQL: $sql2"

        return executeSql { db ->
            db.execute(sql1)
            return executeBuildMols(db, sql2, [smarts])
        }
    }

    List<MoleculeObject> exactSearch(ExactSearch search) {
        return exactSearch(search.smiles, search.chiral, search.limit)
    }

    List<MoleculeObject> exactSearch(String smiles, boolean chiral) {
        return exactSearch(smiles, chiral, -1)
    }

    List<MoleculeObject> exactSearch(String smiles, boolean chiral, int limit) {
        String sql1 = 'SET rdkit.do_chiral_sss=' + chiral
        String sql2 = 'SELECT b.*, m.m FROM ' + baseSchemaPlusTable() + ' b JOIN ' +
                molfpsSchemaPlusTable() + ' m ON b.id = m.id WHERE m.m = ?::mol' +
                (limit == null || limit < 1 ? '' : ' LIMIT ' + limit)
        log.fine "SQL: $sql1"
        log.fine "SQL: $sql2"

        return executeSql { db ->
            db.execute(sql1)
            return executeBuildMols(db, sql2, [smiles])
        }
    }

    private List<MoleculeObject> executeBuildMols(Sql db, String sql, List params) {
        List<MoleculeObject> mols = []
        db.eachRow(sql, params) { row ->
            mols << buildMoleculeObject(row)
        }
        return mols
    }

    private MoleculeObject buildMoleculeObject(def row) {
        MoleculeObject mo = new MoleculeObject(row.structure, molSourceType == MolSourceType.CTAB ? 'mol' : 'smiles')
        mo.putValue('id', row.id)
        mo.putValue(RDKIT_SMILES, row.m)
        extraColumnDefs.keySet().each { k ->
            def o = row[k]
            if (o != null) {
                mo.putValue(k, o)
            }
        }
        return mo
    }

}

