package com.im.lac.chemaxon.molecule;

import chemaxon.formats.MolExporter;
import chemaxon.struc.Molecule;
import com.im.lac.types.MoleculeObject;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Simple de-duplicator that uses unique smiles (smiles:u in ChemAxon notation)
 * to detect duplicates. 
 * When as structure is encountered that is a duplicate of one already encountered
 * it is omitted from the output.
 * You can use parallel streams to speed up processing, but if so then there is no 
 * guarantee that the first structure among duplicates will the the one retained.
 * The use of canonical smiles means that aromatization and explicity hydrogens should
 * be handled, but if you need other processing (mesomers, tautomers, salts, isotopes ...) 
 * you should standardize the structures before de-duplicating.
 * Using canonical smiles does not give a 100% guarantee that structures are identical, 
 * and as the smiles are held in memory during processing it should not be used 
 * for very large data sets.
 * To solve both of these issues a database backed de-duplicator is planned, but this 
 * will perform significantly slower. For most simple cases this SmilesDeduplicator
 * should suffice.
 *
 * @author Tim Dudgeon
 */
public class SmilesDeduplicator {

    private static final Logger LOG = Logger.getLogger(SmilesDeduplicator.class.getName());
    private final boolean storeMolecule, includeErrors, parallel;
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicInteger total = new AtomicInteger(0);
   private final AtomicInteger duplicateCount = new AtomicInteger(0);

    /**
     * Constructor with storeMolecule, includeErrors, parallel all set to false.
     */
    public SmilesDeduplicator() {
        this(false, false, false);
    }

    /**
     * 
     * @param storeMolecule Whether to store the generated Molecule instance on the 
     * MoleculeObject for later use.
     * @param includeErrors Whether to include structures that throw errors in smiles
     * generation in the output
     * @param parallel Whether to use a parallel stream (if possible) which will result 
     * in faster processing but no guarantee that the first structure among duplicates 
     * will the the one retained.
     */
    public SmilesDeduplicator(boolean storeMolecule, boolean includeErrors, boolean parallel) {
        this.storeMolecule = storeMolecule;
        this.includeErrors = includeErrors;
        this.parallel = parallel;
    }
    
    public int getErrorCount() {
        return errorCount.intValue();
    }
    
    public int getDuplicateCount() {
        return duplicateCount.intValue();
    }
    
    public int getTotal() {
        return total.intValue();
    }

    public Stream<MoleculeObject> processStream(Stream<MoleculeObject> mols) {
        final Set<String> smiles = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        
        return prepareStream(mols).filter(mo -> {
            total.incrementAndGet();
            try {
                Molecule mol = MoleculeUtils.fetchMolecule(mo, storeMolecule);
                String usmiles = MolExporter.exportToFormat(mol, "cxsmiles:u");
                //System.out.println(mo.getSource() + " -> " + usmiles);
                if (smiles.contains(usmiles)) {
                    duplicateCount.incrementAndGet();
                    return false;
                } else {
                    smiles.add(usmiles);
                    return true;
                }
            } catch (Exception ex) {
                errorCount.incrementAndGet();
                LOG.log(Level.SEVERE, "Failed to convert to smiles", ex);
                return includeErrors;
            }
        });
    }
    
    private Stream<MoleculeObject> prepareStream(Stream<MoleculeObject> mols) {
        if (parallel) {
            return mols.parallel();
        } else {
            return mols.sequential();
        }
    }
}
