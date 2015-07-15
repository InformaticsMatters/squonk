
package com.im.lac.camel.dataformat;

import com.im.lac.dataset.Metadata;
import com.im.lac.types.MoleculeObject;
import com.im.lac.types.io.MoleculeObjectJsonConverter;
import com.im.lac.util.StreamProvider;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 * Camel DataFormat for MoleculeObjects.
 * 
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

    /**
     * For correct deserialization of values that are not primitive JSON types the 
     * Metadata must be present as a header named "metadata".
     * 
     * @param exchange
     * @param in
     * @return
     * @throws Exception 
     */
    @Override
    public Object unmarshal(Exchange exchange, InputStream in) throws Exception {
        MoleculeObjectJsonConverter unmarshaler = new MoleculeObjectJsonConverter();
        Metadata meta = exchange.getIn().getHeader("metadata", Metadata.class);
        if (meta == null) {
            meta = new Metadata();
            meta.setClassName(MoleculeObject.class.getName());
            meta.setType(Metadata.Type.ARRAY);
        }
        return unmarshaler.unmarshal(meta, in);
    }
    
}
