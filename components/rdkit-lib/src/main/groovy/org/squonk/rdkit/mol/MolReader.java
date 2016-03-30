package org.squonk.rdkit.mol;

import com.im.lac.types.MoleculeObject;
import org.RDKit.ROMol;
import org.RDKit.RWMol;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class MolReader {

    private static final Logger LOG = Logger.getLogger(MolReader.class.getName());

    static {
        System.loadLibrary("GraphMolWrap");
    }

    /**
     * Looks up or creates (and optionally stores under the name org.RDKit.ROMol) the ROMol for this MoleculeObject
     *
     * @param mo
     * @return
     */
    public static ROMol findROMol(MoleculeObject mo, boolean store) {
        String source = mo.getSource();
        if (source == null) {
            return null;
        }
        ROMol rdkitMol = mo.getRepresentation(ROMol.class.getName(), ROMol.class);
        if (rdkitMol == null) {
            String format = mo.getFormat();
            try {
                rdkitMol = MolReader.generateMolFromString(source, format);
                if (store) {
                    mo.putRepresentation(ROMol.class.getName(), rdkitMol);
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to generate RDKit molecule for molecule " + mo.getUUID(), ex);
                return null;
            }
        }
        return rdkitMol;
    }

    public static ROMol findROMol(MoleculeObject mo) {
        return findROMol(mo, true);
    }

    public static RWMol generateMolFromSmiles(String smiles) {
        return RWMol.MolFromSmiles(smiles);
    }

    public static RWMol generateMolFromMolfile(String molfile) {
        return RWMol.MolFromMolBlock(molfile);
    }

    public static RWMol generateMolFromString(String source, String format) {
        RWMol mol = null;
        if (format != null) {
            format = format.toLowerCase();
            if (format.startsWith("smiles") || format.startsWith("cxsmiles")) {
                mol = RWMol.MolFromSmiles(source);
            } else if (format.startsWith("mol")) {
                mol = RWMol.MolFromMolBlock(source, true, false);
            }
        } else {
            //LOG.fine("Trying as Molfile");
            mol = RWMol.MolFromMolBlock(source, true, false);
            if (mol == null) {
                //LOG.fine("Trying as Smiles");
                mol = RWMol.MolFromSmiles(source);
            }
        }
        if (mol != null) {
            return mol;
        } else {
            throw new IllegalArgumentException("RDKit cannot read molecule");
        }
    }

    public static Stream<ROMol> readSmiles(String file, String delimiter, int smilesCol, int nameCol, boolean hasTitleLine, boolean sanitize) {
        return MolSupplierSpliterator.forSmilesFile(file, delimiter, smilesCol, nameCol, hasTitleLine, sanitize).asStream(true);
    }

}
