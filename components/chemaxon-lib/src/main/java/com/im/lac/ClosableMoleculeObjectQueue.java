package com.im.lac;

import chemaxon.formats.MolExporter;
import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class ClosableMoleculeObjectQueue extends ClosableQueue<MoleculeObject> implements MoleculeObjectIterable {

    private static final Logger LOG = Logger.getLogger(ClosableMoleculeObjectQueue.class.getName());

    public ClosableMoleculeObjectQueue(int queueSize) {
        super(queueSize);
    }

    public ClosableMoleculeObjectQueue(BlockingQueue<MoleculeObject> queue) {
        super(queue);
    }

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
                    for (MoleculeObject mo : ClosableMoleculeObjectQueue.this) {
                        try {
                            Molecule mol = MoleculeUtils.fetchMolecule(mo, false);
                            mol.clearProperties();
                            MoleculeUtils.putPropertiesToMolecule(mo.getValues(), mol);
                            exporter.write(mol);
                        } catch (IOException ex) {
                            LOG.log(Level.SEVERE, "Error writing Molecule", ex);
                        }
                    }
                    LOG.fine("Finished to write molecules");
                } finally {
                    try {
                        exporter.close();
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "Failed to close MolExporter", ex);
                    }
                }
            }
        };
        t.start();

        return pis;

    }
}
