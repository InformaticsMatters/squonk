package com.im.lac.rdkit.mol;

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
        if (format != null) {
            format = format.toLowerCase();
            if (format.startsWith("smiles") || format.startsWith("cxsmiles")) {
                return RWMol.MolFromSmiles(source);
            } else if (format.startsWith("mol")) {
                RWMol mol = RWMol.MolFromMolBlock(source);
                return mol;
            }
        } else {
            try {
                LOG.info("Trying as Molfile");
                RWMol mol = RWMol.MolFromMolBlock(source);
                if (mol != null) {
                    return null;
                }
            } catch (Exception ex1) {

            }
            try {
                LOG.info("Trying as Smiles");
                return RWMol.MolFromSmiles(source);
            } catch (Exception ex2) {
                // no joy
                LOG.log(Level.INFO, "Failed to generate Mol: {0}", ex2);
            }

        }
        throw new IllegalArgumentException("Cannot determine format");
    }

    public static Stream<ROMol> readSmiles(String file, String delimiter, int smilesCol, int nameCol, boolean hasTitleLine, boolean sanitize) {
        return MolSupplierSpliterator.forSmilesFile(file, delimiter, smilesCol, nameCol, hasTitleLine, sanitize).asStream(true);
    }

}
