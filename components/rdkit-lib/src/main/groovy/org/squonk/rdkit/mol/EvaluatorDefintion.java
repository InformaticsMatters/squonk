package org.squonk.rdkit.mol;

import org.RDKit.ROMol;

/**
 *
 * @author timbo
 */
public class EvaluatorDefintion {

    public enum Function {

        EXACT_MW(Float.class, "RDKit_ExactMW"),
        FRACTION_C_SP3(Float.class, "RDKit_FracCSP3"),
        LIPINSKI_HBA(Integer.class, "RDKit_Lip_HBA"),
        LIPINSKI_HBD(Integer.class, "RDKit_Lip_HBD"),
        LOGP(Float.class,"RDKit_LogP"),
        MOLECULAR_FORMULA(String.class, "RDKit_MolFormula"),
        MOLAR_REFRACTIVITY(Float.class, "RDKit_MolarRefractivity"),
        NUM_HBA(Integer.class, "RDKit_HBA"),
        NUM_HBD(Integer.class, "RDKit_HBD"),
        NUM_HETEROATOMS(Integer.class, "RDKit_HeteroAtomCount"),
        NUM_RINGS(Integer.class, "RDKit_RingCount"),
        NUM_AROMATIC_RINGS(Integer.class, "RDKit_AromaticRingCount"),
        NUM_ROTATABLE_BONDS(Integer.class, "RDKit_RotBondCount"),
        TPSA(Float.class, "RDKit_TPSA");

        Class type;
        String name;

        Function(Class type, String name) {
            this.type = type;
            this.name = name;
        }

        public Class getType() {
            return type;
        }

        public String getName() {
            return name;
        }

    }

    /**
     * Currently 2 types are supported as defined by the Mode enum.
     *
     * 1. Calculate - calculates a property using the supplied expression and stores it as the
     * supplied propName
     *
     * 2. Filter - includes or excludes the MoleculeObject based of the supplied expression
     *
     * Note: the ChemAxon equivalent also has these modes that may have RDKit equivalents:
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
    public final Function function;

    EvaluatorDefintion(Mode mode, String propName, Function function) {
        this.mode = mode;
        this.propName = propName;
        this.function = function;
    }

    public static EvaluatorDefintion calculate(Function func, String propName) {
        return new EvaluatorDefintion(Mode.Calculate, propName, func);
    }

    @Override
    public String toString() {
        return "EvaluatorDefinition: mode=" + mode + " propName=" + propName + " function=" + function;
    }
    
    
}
