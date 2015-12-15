package com.squonk.db.rdkit

import groovy.sql.Sql
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
abstract class RDKitTable {
    
    static final String RDKIT_SMILES = 'RDKit_smiles'
    
    enum MolSourceType {
        CTAB, SMILES
    }
    
    enum FingerprintType { 
        RDKIT('rdkit_fp(%s)', 'rdk'),
        MORGAN_CONNECTIVITY_2('morganbv_fp(%s,2)', 'mfp2'), 
        MORGAN_CONNECTIVITY_3('morganbv_fp(%s,3)', 'mfp3'), 
        MORGAN_FEATURE_2('featmorganbv_fp(%s,2)', 'ffp2'), 
        MORGAN_FEATURE_3('featmorganbv_fp(%s,3)', 'ffp3'), 
        TORSION('torsionbv_fp(%s)', 'tfp'), 
        MACCS('maccs_fp(%s)', 'maccsfp') 
    
        String function
        String colName
        
        FingerprintType(String function, String col) {
            this.function = function
            this.colName = col
        }
    }
    
    enum Metric { 
        TANIMOTO('%', 'tanimoto_sml(%s)', 'rdkit.tanimoto_threshold'), 
        DICE('#', 'dice_sml(%s)', 'rdkit.dice_threshold') 
    
        String operator
        String function
        String simThresholdProp
        
        Metric(String operator, String function, String simThresholdProp) {
            this.operator = operator
            this.function = function
            this.simThresholdProp = simThresholdProp
        }
    }
    
    protected final DataSource dataSource
    protected final String schema
    protected final String baseTable
    protected final MolSourceType molSourceType = MolSourceType.CTAB
    protected final Map<String,String> extraColumnDefs// = [version_id:'INTEGER', parent_id:'INTEGER']
    
    RDKitTable(DataSource dataSource, String schema, String baseTable, MolSourceType molSourceType, Map<String,String> extraColumnDefs) {
        this.dataSource = dataSource
        this.schema = schema
        this.baseTable = baseTable
        this.molSourceType = molSourceType
        this.extraColumnDefs = extraColumnDefs
    }
    
    
    String getMolfpsTable() {
        return baseTable + "_molfps"
    }
    
    String baseSchemaPlusTable() {
        return schema + '.' + baseTable
    }
    
    String molfpsSchemaPlusTable() {
        return schema + '.' + getMolfpsTable()
    }
    
    protected Sql getSql() {
        return new Sql(dataSource.getConnection())
    }
    
    protected def executeSql(Closure cl) {
        Sql db = getSql()
        try {
            return cl(db)
        } finally {
            db.close()
        }
    }
    
    protected String getSimSearchHelperFunctionName(FingerprintType type, Metric metric) {
        return 'get_' + baseTable + '_' + type.colName + '_neighbours_' + metric.toString().toLowerCase()
    }
    
    String getExtraColumnDefintions(String joinChars) {
        return extraColumnDefs.collect { it.key + ' ' + it.value }.join(joinChars)
    }
	
}

