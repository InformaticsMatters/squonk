package org.squonk.chemaxon.molecule;

import chemaxon.formats.MolExporter;
import chemaxon.struc.Molecule;
import com.im.lac.util.OutputGenerator;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

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
        return getTextStream(format, false);
    }

    @Override
    public InputStream getTextStream(String format, boolean gzip) throws IOException {

        LOG.fine("Creating Text stream");

        final PipedInputStream pis = new PipedInputStream();
        final OutputStream out = new PipedOutputStream(pis);
        final MolExporter exporter = new MolExporter(gzip ? new GZIPOutputStream(out) : out, format);

        Thread t = new Thread() {

            @Override
            public void run() {
                LOG.fine("Starting to write molecules");
                try (Stream<MoleculeObject> seq = mols.sequential()) {

                    seq.forEachOrdered(mo -> {
                        try {
                            Molecule mol = MoleculeUtils.fetchMolecule(mo, false);
                            mol.clearProperties();
                            mol.setProperty("uuid", mo.getUUID().toString());
                            MoleculeUtils.putPropertiesToMolecule(hackValues(mo.getValues()), mol);
                            exporter.write(mol);
                            count++;
                        } catch (IOException ex) {
                            errors++;
                            throw new RuntimeException("Error writing Molecule", ex);
                        }
                    });
                    LOG.fine("Finished writing molecules");
                } finally {
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
    
    /** This hack works round the problem of MolExporter not being able to handle Float values by converting
     * them to Doubles
     * 
     * @param values
     * @return 
     */
    private Map<String,Object> hackValues(Map<String,Object> values) {
        Map<String,Object> neu = new LinkedHashMap<>();
        for (Map.Entry<String,Object> e : values.entrySet()) {
            Object val = e.getValue();
            if (val == null) {
                neu.put(e.getKey(), null);
            } else if (val instanceof Float) {
                neu.put(e.getKey(), ((Float)val).doubleValue());
            } else {
                neu.put(e.getKey(), val);
            }
        }
        return neu;
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
