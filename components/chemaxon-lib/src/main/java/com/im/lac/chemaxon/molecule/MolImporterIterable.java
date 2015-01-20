package com.im.lac.chemaxon.molecule;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 *
 * @author timbo
 */
public class MolImporterIterable implements MoleculeIterable, Closeable {

    final MolImporter importer;

    public MolImporterIterable(InputStream is) throws IOException {
        importer = new MolImporter(is);
    }

    public MolImporterIterable(File file) throws IOException {
        importer = new MolImporter(new FileInputStream(file));
    }

    @Override
    public Iterator<Molecule> iterator() {
        return new MyIteratorDecorator(importer.iterator());
    }

    @Override
    public void close() throws IOException {
        importer.close();
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
            System.out.println("next()");
            return iterator.next();
         }

        @Override
        public void remove() {
            iterator.remove();
        }
        
        
        
    } 

}
