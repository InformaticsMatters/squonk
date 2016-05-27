package org.squonk.rdkit.mol;

/**
 *
 * @author timbo
 */
public class EvaluatorDefintion {

    public enum Function {

        EXACT_MW(Float.class, "ExactMW_RDKit"),
        FRACTION_C_SP3(Float.class, "FracCSP3_RDKit"),
        LIPINSKI_HBA(Integer.class, "HBA_LIP_RDKit"),
        LIPINSKI_HBD(Integer.class, "HBD_Lip_RDKit"),
        LOGP(Float.class,"LogP_RDKit"),
        MOLECULAR_FORMULA(String.class, "MolFormula_RDKit"),
        MOLAR_REFRACTIVITY(Float.class, "MolarRefractivity_RDKit"),
        NUM_HBA(Integer.class, "HBA_RDKit"),
        NUM_HBD(Integer.class, "HBD_RDKit"),
        NUM_HETEROATOMS(Integer.class, "HeteroAtomCount_RDKit"),
        NUM_RINGS(Integer.class, "RingCount_RDKit"),
        NUM_AROMATIC_RINGS(Integer.class, "AromaticRingCount_RDKit"),
        NUM_ROTATABLE_BONDS(Integer.class, "RotBondCount_RDKit"),
        TPSA(Float.class, "TPSA_RDKit"),
        BOND_COUNT(Integer.class, "BondCount_RDKit"),
        ATOM_COUNT(Integer.class, "AtomCount_RDKit"),
        HEAVY_ATOM_COUNT(Integer.class, "HeavyAtomCount_RDKit"),
        FORMAL_CHARGE(Integer.class, "FormalCharge_RDKit");

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
