package foo;

import org.RDKit.RWMol;

public class SimpleRdkitExample2 {
    
    static { System.loadLibrary("GraphMolWrap"); }

    public static void main(String[] args) {

        String smiles = "c1ccccc1";
        RWMol m = RWMol.MolFromSmiles(smiles);
        System.out.println("Read smiles: " + smiles + " Number of atoms: " + m.getNumAtoms());
    }
}
