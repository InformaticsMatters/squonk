package com.im.lac.chemaxon.io;

import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.struc.MProp;
import chemaxon.struc.MPropertyContainer;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.MoleculeObjectIterable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 * Creates Iterables of MoleculeObjects using MRecordReader. Does not create any
 * chemaxon.struc.Molecule instances
 *
 * @author timbo
 */
public class MoleculeObjectIterableImpl implements Iterator<MoleculeObject>, MoleculeObjectIterable {

    MRecordIterator iter;

    public MoleculeObjectIterableImpl(InputStream is) throws IOException {
        this.iter = new MRecordIterator(is);
    }

    public MoleculeObjectIterableImpl(File file) throws IOException {
        this.iter = new MRecordIterator(new FileInputStream(file));
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public MoleculeObject next() {
        MRecord rec = iter.next();
        String mol = rec.getString();
        String format = rec.getInputFormat();
        String name = rec.getMoleculeName();
        MoleculeObject mo = new MoleculeObject(mol, format);
        if (name != null && name.length() > 0) {
            mo.putValue("name", name);
        }
        MPropertyContainer pc = rec.getPropertyContainer();
        String[] keys = pc.getKeys();
        for (int x = 0; x < keys.length; x++) {
            String key = keys[x];
            MProp prop = pc.get(key);
            Serializable ser = null;
            Object o = prop.getPropValue();
            if (o instanceof Serializable) {
                ser = (Serializable) o;
            } else {
                ser = MPropHandler.convertToString(prop, format);
            }
            mo.putValue(key, ser);
        }
        return mo;
    }

    @Override
    public Iterator<MoleculeObject> iterator() {
        return this;
    }

}
