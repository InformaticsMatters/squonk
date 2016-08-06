package org.squonk.rdkit.mol;

import static org.squonk.util.Metrics.*;

/**
 *
 * @author timbo
 */
public class EvaluatorDefinition {

    public enum Function {

        EXACT_MW(Float.class, "ExactMW_RDKit", METRICS_MASS),
        FRACTION_C_SP3(Float.class, "FracCSP3_RDKit", METRICS_FRACTION_C_SP3),
        LIPINSKI_HBA(Integer.class, "HBA_LIP_RDKit", METRICS_HBA),
        LIPINSKI_HBD(Integer.class, "HBD_Lip_RDKit", METRICS_HBD),
        LOGP(Float.class,"LogP_RDKit", METRICS_LOGP),
        MOLECULAR_FORMULA(String.class, "MolFormula_RDKit", METRICS_MOL_FORMULA),
        MOLAR_REFRACTIVITY(Float.class, "MolarRefractivity_RDKit", METRICS_MOLAR_REFRACTIVITY),
        NUM_HBA(Integer.class, "HBA_RDKit", METRICS_HBA),
        NUM_HBD(Integer.class, "HBD_RDKit", METRICS_HBD),
        NUM_HETEROATOMS(Integer.class, "HeteroAtomCount_RDKit", METRICS_ATOM_COUNT),
        NUM_RINGS(Integer.class, "RingCount_RDKit", METRICS_RING_COUNT),
        NUM_AROMATIC_RINGS(Integer.class, "AromaticRingCount_RDKit", METRICS_RING_COUNT),
        NUM_ROTATABLE_BONDS(Integer.class, "RotBondCount_RDKit", METRICS_ROTATABLE_BOND_COUNT),
        TPSA(Float.class, "TPSA_RDKit", METRICS_PSA),
        BOND_COUNT(Integer.class, "BondCount_RDKit", METRICS_BOND_COUNT),
        ATOM_COUNT(Integer.class, "AtomCount_RDKit", METRICS_ATOM_COUNT),
        HEAVY_ATOM_COUNT(Integer.class, "HeavyAtomCount_RDKit", METRICS_ATOM_COUNT),
        FORMAL_CHARGE(Integer.class, "FormalCharge_RDKit", METRICS_CHARGE);

        Class type;
        String name;
        String metricsCode;

        Function(Class type, String name, String metricsCode) {
            this.type = type;
            this.name = name;
            this.metricsCode = metricsCode;
        }

        public Class getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getMetricsCode() {
            return metricsCode;
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

    EvaluatorDefinition(Mode mode, String propName, Function function) {
        this.mode = mode;
        this.propName = propName;
        this.function = function;
    }

    public static EvaluatorDefinition calculate(Function func, String propName) {
        return new EvaluatorDefinition(Mode.Calculate, propName, func);
    }

    @Override
    public String toString() {
        return "EvaluatorDefinition: mode=" + mode + " propName=" + propName + " function=" + function;
    }
    
    
}
