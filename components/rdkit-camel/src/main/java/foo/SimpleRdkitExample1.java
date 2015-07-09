package foo;

import org.RDKit.RWMol;

public class SimpleRdkitExample1 {
    
    static { System.loadLibrary("GraphMolWrap"); }

    public static void main(String[] args) {
        
        //String property = System.getProperty("java.library.path");
        //System.out.println("java.library.path = " + property);

        //System.loadLibrary("GraphMolWrap");
        String smiles = "c1ccccc1";
        RWMol m = RWMol.MolFromSmiles(smiles);
        System.out.println("Read smiles: " + smiles + " Number of atoms: " + m.getNumAtoms());
    }
}
