package com.im.lac.rdkit.mol;

/**
 *
 * @author timbo
 */
public class EvaluatorDefintion {

    public enum Function {

        EXACT_MW, FRACTION_C_SP3, LIPINSKI_HBA, LIPINSKI_HBD, LOGP, MOLECULAR_FORMULA, MOLAR_REFRACTIVITY, 
        NUM_HBA, NUM_HBD, NUM_HETEROATOMS, NUM_RINGS, NUM_AROMATIC_RINGS, NUM_ROTATABLE_BONDS,
        TPSA
    }

    /**
     * Currently 2 types are supported as defined by the Mode enum.
     *
     * 1. Calculate - calculates a property using the supplied expression and stores it as the
     * supplied propName
     *
     * 2. Filter - includes or excludes the MoleculeObject based of the supplied expression
     *
     * Note: the ChemAxon equivalent also has these modes that may have RDkit equivalents:
     *
     * 3. Transform - convert the molecule in some way e.g. leconformer()
     *
     * 4. Standardize - e.g. aromatize..removeExplicitH
     *
     */
    public enum Mode {

        Calculate, Filter
    };

    public final Mode mode;
    public final String propName;
    public final String expression;

    EvaluatorDefintion(Mode mode, String propName, String expression) {
        this.mode = mode;
        this.propName = propName;
        this.expression = expression;
    }

    public static EvaluatorDefintion calculate(String name, String expression) {
        return new EvaluatorDefintion(Mode.Calculate, name, expression);
    }

    public static EvaluatorDefintion filter(String expression) {
        return new EvaluatorDefintion(Mode.Filter, null, expression);
    }

    @Override
    public String toString() {
        return "EvaluatorDefinition: mode=" + mode + " propName=" + propName + " expression=" + expression; 
    }
    
    
}
