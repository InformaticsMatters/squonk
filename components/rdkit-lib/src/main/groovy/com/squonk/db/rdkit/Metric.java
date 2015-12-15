package com.squonk.db.rdkit;

/**
 * Created by timbo on 13/12/2015.
 */
public enum Metric {
    TANIMOTO("%", "tanimoto_sml(%s)", "rdkit.tanimoto_threshold"),
    DICE("#", "dice_sml(%s)", "rdkit.dice_threshold");

    public String operator;
    public String function;
    public String simThresholdProp;

    Metric(String operator, String function, String simThresholdProp) {
        this.operator = operator;
        this.function = function;
        this.simThresholdProp = simThresholdProp;
    }
}
