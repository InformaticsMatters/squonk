package com.im.lac.camel.chemaxon.converters;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.io.MoleculeIOUtils;
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
    public static Molecule convert(String s, Exchange exchange) throws MolFormatException {
        return MoleculeIOUtils.convertToMolecule(s);
    }

    @Converter
    public static Molecule convert(byte[] bytes, Exchange exchange) throws MolFormatException {
        return MoleculeIOUtils.convertToMolecule(bytes);
    }

    @Converter
    public static Molecule convert(Blob blob, Exchange exchange)
            throws MolFormatException, SQLException {
        return MoleculeIOUtils.convertToMolecule(blob);
    }

    @Converter
    public static Molecule convert(Clob clob, Exchange exchange)
            throws MolFormatException, SQLException {
        return MoleculeIOUtils.convertToMolecule(clob);
    }
    
    
    @Converter
    public static Iterator<Molecule> convert(InputStream is, Exchange exchange) 
            throws MolFormatException, IOException {
        return MoleculeIOUtils.moleculeIterator(is);
    }

}
