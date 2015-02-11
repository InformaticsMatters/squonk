package com.im.lac.camel.dataformat;

import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import groovy.json.JsonLexer;
import groovy.json.JsonOutput;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 * WARNING - this class is incomplete. Do not use.
 * 
 * TODO - move this out of the chemaxon-camel module as it has nothing to do
 * with ChemAxon, but currently doesn't have a better place to live.
 *
 * @author timbo
 */
public class MoleculeObjectJsonDataFormat implements DataFormat {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectJsonDataFormat.class.getName());

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
            mols = Collections.singletonList((MoleculeObject) o).iterator();
        } else {
            throw new IllegalArgumentException("Bad format. Can't handle " + o.getClass().getName());
        }

        try {
            while (mols.hasNext()) {
                MoleculeObject mo = mols.next();
                marshalMoleculeObject(mo, out);
            }
        } finally {
            if (mols instanceof Closeable) {
                try {
                    ((Closeable) mols).close();
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, "Failed to close iterator", ioe);
                }
            }
        }
    }

    void marshalMoleculeObject(MoleculeObject mo, OutputStream out) throws IOException {
        Map data = new LinkedHashMap();
        String format = mo.getFormat();
        if (format != null) {
            data.put("format", format);
        }
        String mol = mo.getSourceAsString();
        if (mol != null) {
            data.put("source", mol);
        }
        if (mo.getValues().size() > 0) {
            data.put("values", mo.getValues());
        }
        String json = JsonOutput.toJson(data);
        out.write(json.getBytes());
    }

    @Override
    public Object unmarshal(Exchange exchange, InputStream is) throws Exception {

        Reader reader = new InputStreamReader(is);
        JsonLexer lexer = new JsonLexer(reader);
        MoleculeObject mo;
        while (null != (mo = unmarshalNext(lexer))) {
            LOG.info("Read MO: " + mo.getSourceAsString());
        }
        

        return null;
        // TODO - implement reading
    }
    
    MoleculeObject unmarshalNext(JsonLexer lexer) {
        
        /*
        Type: OPEN_CURLY Value: {
Type: STRING Value: "format"
Type: COLON Value: :
Type: STRING Value: "smiles"
Type: COMMA Value: ,
Type: STRING Value: "source"
Type: COLON Value: :
Type: STRING Value: "CC1=CC(=O)C=CC1=O
"
Type: COMMA Value: ,
Type: STRING Value: "values"
Type: COLON Value: :
Type: OPEN_CURLY Value: {
Type: STRING Value: "field_0"
Type: COLON Value: :
Type: STRING Value: "1"
Type: CLOSE_CURLY Value: }
Type: CLOSE_CURLY Value: }
Type: COMMA Value: ,
        */
        
        
        while (lexer.hasNext()) {
            
        }
        
        
        MoleculeObject mo = new MoleculeObject();
        
        
        
        return mo;
    }
 
}
