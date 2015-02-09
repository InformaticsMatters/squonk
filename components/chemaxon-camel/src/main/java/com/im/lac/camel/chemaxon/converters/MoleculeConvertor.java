package com.im.lac.camel.chemaxon.converters;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import com.im.lac.util.OutputGenerator;
import com.im.lac.chemaxon.molecule.MoleculeIterable;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.chemaxon.molecule.MoleculeObjectWriter;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Iterator;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;

/**
 * Created by timbo on 21/04/2014.
 */
@Converter
public class MoleculeConvertor {

    @Converter
    public static Molecule convertToMolecule(MoleculeObject mo, Exchange exchange) throws MolFormatException {
        return MoleculeUtils.fetchMolecule(mo, false);
    }
    
    @Converter
    public static MoleculeObject convertToMoleculeObject(String s, Exchange exchange) {
        return new MoleculeObject(s);
    }

    @Converter
    public static Molecule convertToMolecule(String s, Exchange exchange) throws MolFormatException {
        return MoleculeUtils.convertToMolecule(s);
    }
    
    @Converter
    public static Molecule convertToMolecule(byte[] bytes, Exchange exchange) throws MolFormatException {
        return MoleculeUtils.convertToMolecule(bytes);
    }

    @Converter
    public static Molecule convertToMolecule(Blob blob, Exchange exchange)
            throws MolFormatException, SQLException {
        return MoleculeUtils.convertToMolecule(blob);
    }

    @Converter
    public static Molecule convertToMolecule(Clob clob, Exchange exchange)
            throws MolFormatException, SQLException {
        return MoleculeUtils.convertToMolecule(clob);
    }


    @Converter
    public static Iterator<Molecule> createMoleculeIterator(InputStream is, Exchange exchange)
            throws IOException {
        return createMoleculeIterable(is, exchange).iterator();
    }

    @Converter
    public static MoleculeIterable createMoleculeIterable(InputStream is, Exchange exchange)
            throws IOException {
        return MoleculeUtils.createIterable(is);
    }
}
