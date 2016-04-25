package org.squonk.rdkit.db;

/**
 * Created by timbo on 13/12/2015.
 */
public enum FingerprintType {
    RDKIT("rdkit_fp(%s)", "rdk"),
    MORGAN_CONNECTIVITY_2("morganbv_fp(%s,2)", "mfp2"),
    MORGAN_CONNECTIVITY_3("morganbv_fp(%s,3)", "mfp3"),
    MORGAN_FEATURE_2("featmorganbv_fp(%s,2)", "ffp2"),
    MORGAN_FEATURE_3("featmorganbv_fp(%s,3)", "ffp3"),
    TORSION("torsionbv_fp(%s)", "tfp"),
    MACCS("maccs_fp(%s)", "maccsfp");

    public String function;
    public String colName;

    FingerprintType(String function, String col) {
        this.function = function;
        this.colName = col;
    }
}
