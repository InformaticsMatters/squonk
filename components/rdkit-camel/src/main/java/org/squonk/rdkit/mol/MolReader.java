package org.squonk.rdkit.mol;

import com.im.lac.types.MoleculeObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.RDKit.ROMol;
import org.RDKit.RWMol;

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
     * Looks up or creates (and sets) the ROMol for this MoleculeObject
     *
     * @param mo
     * @return
     */
    public static ROMol findROMol(MoleculeObject mo) {
        ROMol rdkitMol = mo.getRepresentation(ROMol.class.getName(), ROMol.class);
        if (rdkitMol == null) {
            String source = mo.getSource();
            String format = mo.getFormat();
            try {
                rdkitMol = MolReader.generateMolFromString(source, format);
                mo.putRepresentation(ROMol.class.getName(), rdkitMol);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to generate RDKit molecule for molecule " + mo.getUUID(), ex);
                return null;
            }
        }
        return rdkitMol;
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
