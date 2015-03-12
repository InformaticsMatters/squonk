package com.im.lac.chemaxon.molecule;

import chemaxon.formats.MolExporter;
import chemaxon.struc.Molecule;
import com.im.lac.util.OutputGenerator;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class MoleculeObjectWriter implements OutputGenerator {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectWriter.class.getName());
    private final Stream<MoleculeObject> mols;
    private int count = 0;
    private int errors = 0;

    public MoleculeObjectWriter(Stream<MoleculeObject> mols) {
        this.mols = mols;
    }

    @Override
    public InputStream getTextStream(String format) throws IOException {

        LOG.fine("Creating Text stream");

        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(pis);
        final MolExporter exporter = new MolExporter(out, format);

        Thread t = new Thread() {

            @Override
            public void run() {
                LOG.fine("Starting to write molecules");
                try {
                    mols.sequential().forEachOrdered(mo -> {
                        try {
                            Molecule mol = MoleculeUtils.fetchMolecule(mo, false);
                            mol.clearProperties();
                            MoleculeUtils.putPropertiesToMolecule(mo.getValues(), mol);
                            exporter.write(mol);
                            count++;
                        } catch (IOException ex) {
                            errors++;
                            throw new RuntimeException("Error writing Molecule", ex);
                        }
                    });
                    LOG.fine("Finished writing molecules");
                } finally {
                    mols.close();
                    try {
                        exporter.close();
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "Failed to close input", ex);
                    }
                }
            }
        };
        t.start();

        return pis;

    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @return the errors
     */
    public int getErrors() {
        return errors;
    }

}
