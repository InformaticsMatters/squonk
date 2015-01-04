package com.im.lac.chemaxon.molecule;

import chemaxon.calculations.clean.Cleaner;
import chemaxon.calculations.hydrogenize.Hydrogenize;
import chemaxon.formats.MolExporter;
import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;
import chemaxon.struc.MoleculeGraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author timbo
 */
public class MoleculeUtils {

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

    /** Export the molecule in text format using the formats specified. They are tried 
     * in order (left to right) and the first one that works is used.  
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
}
