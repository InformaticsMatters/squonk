package org.squonk.rdkit.io;

import com.im.lac.types.MoleculeObject;
import org.RDKit.ROMol;
import org.RDKit.SDWriter;
import org.RDKit.SWIGTYPE_p_std__ostream;
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

}
