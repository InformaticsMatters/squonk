package com.im.lac.camel.dataformat;

import chemaxon.formats.MolExporter;
import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 *
 * @author timbo
 */
public class MoleculeObjectIteratorDataFormat implements DataFormat {
    
    private static final Logger LOG = Logger.getLogger(MoleculeObjectIteratorDataFormat.class.getName());

    @Override
    public void marshal(Exchange exchange, Object o, OutputStream out) throws Exception {
        Iterator<MoleculeObject> mols = null;
        if (o instanceof MoleculeObjectIterable) {
            mols = ((MoleculeObjectIterable) o).iterator();
        } else if (o instanceof Iterator) {
            mols = (Iterator<MoleculeObject>) o;
        } else if (o instanceof Iterable) {
            mols = ((Iterable) o).iterator();
        } else if (o instanceof MoleculeObject) {
            mols = Collections.singletonList((MoleculeObject)o).iterator();
        } else {
            throw new IllegalArgumentException("Bade format. Can't handle " + o.getClass().getName());
        }
        MolExporter exporter = new MolExporter(out, "sdf");
        try {
            while (mols.hasNext()) {
                MoleculeObject mo = mols.next();
                Molecule mol = MoleculeUtils.fetchMolecule(mo, false);
                MoleculeUtils.putPropertiesToMolecule(mo.getValues(), mol);
                exporter.write(mol);
                mol.clearProperties();
            }
        } finally {
            exporter.close();
            if (mols instanceof Closeable) {
            try {
                ((Closeable) mols).close();
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "Failed to close iterator", ioe);
            }
        }
        }
    }

    @Override
    public Object unmarshal(Exchange exchange, InputStream is) throws Exception {
        Iterable it = MoleculeObjectUtils.createIterable(is);
        return it.iterator();
    }

}
