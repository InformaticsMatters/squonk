package org.squonk.rdkit.io;

import com.im.lac.types.MoleculeObject;
import org.RDKit.*;
import org.squonk.rdkit.mol.MolReader;
import org.squonk.types.RDKitSDFile;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 30/03/16.
 */
public class RDKitMoleculeIOUtils {

    private static final Logger LOG = Logger.getLogger(RDKitMoleculeIOUtils.class.getName());

    public enum FragmentMode {
        WHOLE_MOLECULE,
        BIGGEST_BY_ATOM_COUNT,
        BIGGEST_BY_HEAVY_ATOM_COUNT,
        BIGGEST_BY_MOLWEIGHT,
    }

    public static RDKitSDFile covertToSDFile(Stream<MoleculeObject> mols, boolean haltOnError) throws IOException {
        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);

        Thread t = new Thread() {
            public void run() {
                SWIGTYPE_p_std__ostream stream = null; // TODO - work out how to handle this
                try  {
                    SDWriter writer = new SDWriter(stream);
                    mols.forEachOrdered((mo) -> {
                        try {
                            ROMol mol = MolReader.findROMol(mo, false);
                            for (Map.Entry<String, Object> e : mo.getValues().entrySet()) {
                                String key = e.getKey();
                                Object val = e.getValue();
                                if (key != null && val != null) {
                                    mol.setProp(key, val.toString());
                                }
                            }
                            //LOG.info("WRITING MOL");
                            writer.write(mol);
                        } catch (Exception e) {
                            if (haltOnError) {
                                throw new RuntimeException("Failed to read molecule " + mo.getUUID(), e);
                            } else {
                                LOG.warning("Failed to read molecule " + mo.getUUID());
                            }
                        }
                    });
                    LOG.fine("Writing to SDF complete");
                    mols.close();
                } finally {
                    // close the stream
                }
            }
        };
        t.start();

        return new RDKitSDFile(in);
    }

    /** Generate canonical smiles for the molecule, including the ability to specify which fragment to use for molecules with
     * multiple fragments.
     *
     * Note: the behaviour is unrepedictable in the case of molecule with fragments that are different but evaluate the
     * same according to the specified metric. This might be improved in future.
     *
     *
     *
     * @param mo The molecule to canonicalise
     * @param mode
     * @return
     */
    public static String generateCanonicalSmiles(MoleculeObject mo, FragmentMode mode) {
        ROMol mol = MolReader.findROMol(mo, false);
        if (mol == null) {
            return null;
        }
        String smiles = null;
        ROMol_Vect frags = RDKFuncs.getMolFrags(mol);
        if (frags.size() == 1) {
            smiles = mol.MolToSmiles(true);
        } else if (frags.size() > 1) {

            long atoms = 0;
            ROMol biggest = null;

            switch (mode) {

                case BIGGEST_BY_ATOM_COUNT:
                    for (int i=0; i<frags.size(); i++) {
                        ROMol frag = frags.get(i);
                        long current = frag.addHs(false).getNumAtoms();
                        if (current > atoms) {
                            atoms = current;
                            biggest = frag;
                        }
                    }
                    if (biggest != null) {
                        smiles = biggest.MolToSmiles(true);
                    }
                    break;

                case BIGGEST_BY_HEAVY_ATOM_COUNT:

                    for (int i=0; i<frags.size(); i++) {
                        ROMol frag = frags.get(i);
                        long current = frag.getNumHeavyAtoms();
                        if (current > atoms) {
                            atoms = current;
                            biggest = frag;
                        }
                    }
                    if (biggest != null) {
                        smiles = biggest.MolToSmiles(true);
                    }
                    break;

                case BIGGEST_BY_MOLWEIGHT:
                    double mw = 0;
                    for (int i=0; i<frags.size(); i++) {
                        ROMol frag = frags.get(i);
                        double current = RDKFuncs.calcExactMW(frag);
                        if (current > mw) {
                            mw = current;
                            biggest = frag;
                        }
                    }
                    if (biggest != null) {
                        smiles = biggest.MolToSmiles(true);
                    }
                    break;

                default:
                    smiles = mol.MolToSmiles(true);

            }
        }

        return smiles;
    }

}
