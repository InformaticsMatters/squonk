package com.im.lac.chemaxon.io;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.molecule.MoleculeIterable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/** Creates Iterables of Molecules using MolImporter
 *
 * @author timbo
 */
public class MoleculeIterableImpl implements MoleculeIterable {

    private final MolImporter importer;

    public MoleculeIterableImpl(InputStream is) throws IOException {
        importer = new MolImporter(is);
    }

    public MoleculeIterableImpl(File file) throws IOException {
        importer = new MolImporter(new FileInputStream(file));
    }

    @Override
    public Iterator<Molecule> iterator() {
        return new MyIteratorDecorator(importer.iterator());
    }
    
    class MyIteratorDecorator implements Iterator<Molecule> {
        
        Iterator<Molecule> iterator;
        MyIteratorDecorator(Iterator<Molecule> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Molecule next() {
            return iterator.next();
         }

        @Override
        public void remove() {
            iterator.remove();
        }
        
    } 

}
