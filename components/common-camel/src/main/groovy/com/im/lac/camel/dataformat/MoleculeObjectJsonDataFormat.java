
package com.im.lac.camel.dataformat;

import com.im.lac.util.MoleculeObjectJsonConverter;
import com.im.lac.util.StreamProvider;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 *
 * @author timbo
 */
public class MoleculeObjectJsonDataFormat implements DataFormat {
    
    

    @Override
    public void marshal(Exchange exchng, Object o, OutputStream out) throws Exception {
        MoleculeObjectJsonConverter marshaler = new MoleculeObjectJsonConverter();
        StreamProvider sp = exchng.getContext().getTypeConverter().mandatoryConvertTo(StreamProvider.class, o);
        marshaler.marshal(sp.getStream(), out);
    }

    @Override
    public Object unmarshal(Exchange exchng, InputStream in) throws Exception {
        MoleculeObjectJsonConverter unmarshaler = new MoleculeObjectJsonConverter();
        return unmarshaler.unmarshal(in);
    }
    
}
