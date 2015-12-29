package com.im.lac.rdkit.mol;

import org.squonk.stream.FixedBatchSpliteratorBase;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.RDKit.MolSupplier;
import org.RDKit.ROMol;
import org.RDKit.SDMolSupplier;
import org.RDKit.SmilesMolSupplier;

/**
 *
 * @author timbo
 */
public class MolSupplierSpliterator extends FixedBatchSpliteratorBase<ROMol> {
    
    private static final Logger LOG = Logger.getLogger(MolSupplierSpliterator.class.getName());

    private final MolSupplier supplier;

    protected MolSupplierSpliterator(MolSupplier supplier) {
        this.supplier = supplier;
    }

    public static MolSupplierSpliterator forSmilesFile(String file) {
        return new MolSupplierSpliterator(new SmilesMolSupplier(file));
    }

    public static MolSupplierSpliterator forSmilesFile(String file, String delimiter, int smilesCol, int nameCol, boolean hasTitleLine, boolean sanitize) {
        return new MolSupplierSpliterator(new SmilesMolSupplier(file, delimiter, smilesCol, nameCol, hasTitleLine, sanitize));
    }

    public static MolSupplierSpliterator forSDFile(String file) {
        return new MolSupplierSpliterator(new SDMolSupplier(file));
    }

    @Override
    public boolean tryAdvance(Consumer<? super ROMol> action) {
        if (supplier.atEnd()) {
            return false;
        }
        ROMol mol = supplier.next();
        //LOG.info("Read molecule " + mol.MolToSmiles());
        action.accept(mol);
        return true;
    }

    /**
     * Generates a Stream from this Spliterator.
     *
     * @param parallel Should the returned stream be a parallel stream
     * @return
     */
    public Stream<ROMol> asStream(boolean parallel) {
        return StreamSupport.stream(this, parallel);
    }

}
