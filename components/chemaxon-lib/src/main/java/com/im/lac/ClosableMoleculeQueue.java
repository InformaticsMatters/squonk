/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac;

import chemaxon.formats.MolExporter;
import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.molecule.MoleculeIterable;
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
public class ClosableMoleculeQueue extends ClosableQueue<Molecule> implements MoleculeIterable {

    private static final Logger LOG = Logger.getLogger(ClosableMoleculeQueue.class.getName());

    public ClosableMoleculeQueue(int queueSize) {
        super(queueSize);
    }

    public ClosableMoleculeQueue(BlockingQueue<Molecule> queue) {
        super(queue);
    }

    public InputStream getTextStream(String format) throws IOException {
        
        LOG.info("Creating Text stream");

        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(pis);
        final MolExporter exporter = new MolExporter(out, format);

        Thread t = new Thread() {

            @Override
            public void run() {
                LOG.fine("Starting to write molecules");
                try {
                    for (Molecule mol : ClosableMoleculeQueue.this) {
                        try {
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
