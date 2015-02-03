package com.im.lac.chemaxon.molecule;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * Creates MoleculeIterables using MolImporter for import where necessary.
 *
 * @author timbo
 */
public class MoleculeFactory {

    public static MoleculeIterable createIterable(InputStream is) throws IOException {
        return new IteratorAdapter(new MolImporter(is));
    }

    public static MoleculeIterable createIterable(File file) throws IOException {
        return new IteratorAdapter(new MolImporter(file));
    }

    public static MoleculeIterable createIterable(Collection<Molecule> mols) {
        return new CollectionAdapter(mols);
    }

    static class CollectionAdapter implements MoleculeIterable {

        final Collection<Molecule> mols;

        CollectionAdapter(Collection<Molecule> mols) {
            this.mols = mols;
        }

        @Override
        public Iterator<Molecule> iterator() {
            return mols.iterator();
        }
    }

    static class IteratorAdapter implements MoleculeIterable, Closeable {

        final Iterator<Molecule> iterator;
        final MolImporter importer;

        IteratorAdapter(Iterator<Molecule> iterator) {
            this.iterator = iterator;
            importer = null;
        }

        IteratorAdapter(MolImporter importer) {
            this.iterator = importer.iterator();
            this.importer = importer;
        }

        @Override
        public Iterator<Molecule> iterator() {
            return iterator;
        }

        @Override
        public void close() throws IOException {
            if (importer != null) {
                importer.close();
            }
        }

    }

}
