package com.im.lac.chemaxon.molecule;

import chemaxon.calculations.clean.Cleaner;
import chemaxon.calculations.hydrogenize.Hydrogenize;
import chemaxon.formats.MolExporter;
import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.struc.MProp;
import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;
import chemaxon.struc.MoleculeGraph;
import static com.im.lac.chemaxon.molecule.MoleculeConstants.STRUCTURE_FIELD_NAME;
import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class MoleculeUtils {
    
    static Logger LOG = Logger.getLogger(MoleculeUtils.class.getName());

    public String cleanOpts = null;
    public int removeExplicityHFlags = MolAtom.LONELY_H | MolAtom.WEDGED_H;
    public String exportFormat = "sdf";

    static public int heavyAtomCount(MoleculeGraph mol) {
        int count = 0;
        for (MolAtom atom : mol.getAtomArray()) {
            if (atom.getAtno() > 1) {
                count++;
            }
        }
        return count;
    }

    public MoleculeGraph clean(MoleculeGraph mol, int dim, String opts) {
        Cleaner.clean(mol, dim, opts);
        return mol;
    }

    public MoleculeGraph clean2d(MoleculeGraph mol) {
        clean(mol, 2, cleanOpts);
        return mol;
    }

    public MoleculeGraph clean3d(MoleculeGraph mol) {
        clean(mol, 3, cleanOpts);
        return mol;
    }

    public MoleculeGraph removeExplicitH(MoleculeGraph mol) {
        Hydrogenize.convertExplicitHToImplicit(mol, removeExplicityHFlags);
        return mol;
    }

    public String exportAsString(Molecule mol) throws IOException {
        return MolExporter.exportToFormat(mol, exportFormat);
    }

    /**
     * Export the molecule in text format using the formats specified. They are
     * tried in order (left to right) and the first one that works is used.
     *
     * @param mol The molecule to convert
     * @param format The formats to try
     * @return
     * @throws IOException Thrown if none of the specified formats work
     */
    public static String exportAsString(Molecule mol, String... format) throws IOException {
        IOException ex = null;
        for (String f : format) {
            try {
                return MolExporter.exportToFormat(mol, f);
            } catch (IOException e) {
                ex = e;
            }
        }
        throw ex;
    }

    /**
     * Finds the parent structure. If there is only one fragment it returns the
     * input molecule (same instance). If there are multiple fragments if
     * returns the biggest by atom count. If multiple fragments have the same
     * number of atoms then the one with the biggest mass is returned. If
     * multiple ones have the same atom count and mass it is assumed they are
     * the same (which is not necessarily the case) and the first is returned.
     *
     *
     * @param mol The molecule to examine
     * @return The parent fragment, or null if none can be found
     */
    public static Molecule findParentStructure(Molecule mol) {
        Molecule[] frags = mol.cloneMolecule().convertToFrags();
        if (frags.length == 1) {
            return mol; // the orginal molecule
        } else {
            int maxAtoms = 0;
            List<Molecule> biggestByAtomCount = new ArrayList<Molecule>();
            for (Molecule f : frags) {
                int ac = f.getAtomCount() + f.getImplicitHcount();
                if (ac > maxAtoms) {
                    biggestByAtomCount.clear();
                    biggestByAtomCount.add(f);
                    maxAtoms = ac;
                } else if (f.getAtomCount() == maxAtoms) {
                    biggestByAtomCount.add(f);
                }
            }
            if (biggestByAtomCount.size() == 1) {
                return biggestByAtomCount.get(0);
            } else {
                List<Molecule> biggestByMass = new ArrayList<Molecule>();

                double maxMass = 0;
                for (Molecule f : biggestByAtomCount) {
                    double mass = f.getMass();
                    if (mass > maxMass) {
                        biggestByMass.clear();
                        biggestByMass.add(f);
                        maxMass = mass;
                    } else if (f.getMass() == maxMass) {
                        biggestByMass.add(f);
                    }
                }
                if (biggestByMass.size() > 0) {
                    return biggestByMass.get(0);
                } else { // strange?
                    return null;
                }
            }
        }
    }

    /**
     * Fetch the Molecule stored under the name chemaxon.struc.Molecule,
     * creating it if its not already present, and optionally putting it under
     * that name
     *
     * @param mo
     * @param store
     * @return
     */
    public static Molecule fetchMolecule(MoleculeObject mo, boolean store) {
        Molecule mol = mo.getRepresentation(Molecule.class, Molecule.class);
        if (mol == null) {
            try {
                mol = convertToMolecule(mo.getSourceAsBytes());
            } catch (MolFormatException ex) {
                throw new RuntimeException("Bad format for Molecule", ex);
            }
            if (store) {
                mo.putRepresentation(Molecule.class.getName(), mol);
            }
        }
        return mol;
    }

    public static MoleculeObject derriveMoleculeObject(MoleculeObject old, Molecule neu, String format)
            throws IOException {
        String s = MoleculeUtils.exportAsString(neu, format);
        MoleculeObject mo = new MoleculeObject(s, format);
        mo.putRepresentation(Molecule.class.getName(), neu);
        mo.putValues(old.getValues());

        return mo;
    }
    
    public static MoleculeObject createMoleculeObject(Molecule mol, String format)
            throws IOException {
        String s = MoleculeUtils.exportAsString(mol, format);
        MoleculeObject mo = new MoleculeObject(s, format);
        for (String key: mol.properties().getKeys()) {
            Object val = mol.getPropertyObject(key);
            mo.putValue(key, val);
        }
        mol.clearProperties();
        mo.putRepresentation(Molecule.class.getName(), mol);
        return mo;
    }

    public static void putPropertiesToMolecule(Map<? extends Object,? extends Object> props, Molecule mol) {

        for (Map.Entry e : props.entrySet()) {
            mol.setPropertyObject(e.getKey().toString(), e.getValue());
        }
    }
    
     /**
     * Creates an Iterator of MRecords from the InputStream Designed for
     * splitting a file or stream into individual records without generating a
     * molecule instance. Can be used as a Camel splitter.
     * <code>split().method(MoleculeIOUtils.class, "mrecordIterator")</code>
     *
     * @param is The input molecules in any format that Marvin recognises
     * @return Iterator or records
     * @throws IOException
     */
    public static Iterator<MRecord> mrecordIterator(final InputStream is) throws IOException {
        LOG.log(Level.FINE, "Creating Iterator<MRecord> for %s", is.getClass().getName());
        return new MRecordIterator(is);
    }


    public static Map<String, String> mrecordToMap(MRecord record) {
        Map<String, String> vals = new HashMap<>();
        vals.put(STRUCTURE_FIELD_NAME, record.getString());
        String[] fields = record.getPropertyContainer().getKeys();
        List<MProp> values = record.getPropertyContainer().getPropList();
        for (int x = 0; x < fields.length; x++) {
            vals.put(fields[x], MPropHandler.convertToString(values.get(x), null));
        }
        return vals;
    }


    public static Molecule convertToMolecule(String s) throws MolFormatException {
        return MolImporter.importMol(s);
    }

    public static Molecule convertToMolecule(byte[] b) throws MolFormatException {
        return MolImporter.importMol(b);
    }

    public static Molecule convertToMolecule(Blob b) throws MolFormatException, SQLException {
        byte[] bytes = b.getBytes(1, (int) b.length());
        return MolImporter.importMol(bytes);
    }

    public static Molecule convertToMolecule(Clob c) throws MolFormatException, SQLException {
        String s = c.getSubString(1, (int) c.length());
        return convertToMolecule(s);
    }

    /**
     * Takes a Map of properties, one of which is a Molecule in some form and
     * returns a Molecule (converted as necessary) with the additional values
     * from the Map set as properties of the Molecule (see
     * Molecule.getProperties()).
     *
     * @param map The input
     * @param structureKey The key under which the structure is located
     * @return The Molecule
     * @throws MolFormatException
     * @throws SQLException
     */
    public static Molecule convertToMolecule(Map<String, Object> map, String structureKey) throws MolFormatException, SQLException {

        Object v = map.get(structureKey);
        Molecule mol = null;
        if (v.getClass() == String.class) {
            mol = convertToMolecule((String) v);
        } else if (v.getClass() == byte[].class) {
            mol = convertToMolecule((byte[]) v);
        } else if (v.getClass() == Blob.class) {
            mol = convertToMolecule((Blob) v);
        } else if (v.getClass() == Clob.class) {
            mol = convertToMolecule((Clob) v);
        } else if (v.getClass() == Molecule.class) {
            mol = (Molecule) v;
        } else {
            throw new IllegalArgumentException("Unsupported conversion for Molecule: "
                    + v.getClass().getName());
        }

        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!e.getKey().equals(structureKey)) {
                mol.properties().setObject(e.getKey(), e.getValue());
            }
        }

        return mol;
    }
}
