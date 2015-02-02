package com.im.lac.camel.dataformat;

import chemaxon.formats.MolExporter;
import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 *
 * @author timbo
 */
public class MoleculeIteratorDataFormat implements DataFormat {

    @Override
    public void marshal(Exchange exchange, Object o, OutputStream out) throws Exception {
        Iterator<Molecule> mols = null;
        if (o instanceof Iterator) {
            mols = (Iterator<Molecule>) o;
        } else if (o instanceof Iterable) {
            mols = ((Iterable) o).iterator();
        } else if (o instanceof Molecule) {
            mols = Collections.singletonList((Molecule)o).iterator();
        }
        MolExporter exporter = new MolExporter(out, "sdf");
        try {
            while (mols.hasNext()) {
                exporter.write(mols.next());
            }
        } finally {
            exporter.close();
        }
    }

    @Override
    public Object unmarshal(Exchange exchange, InputStream in) throws Exception {
        Iterator it = MoleculeUtils.moleculeIterator(in);
        System.out.println("Iterator is " + it);
        return it;
    }

}
