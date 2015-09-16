package com.im.lac.camel.dataformat;

import com.im.lac.types.MoleculeObject;
import com.im.lac.util.StreamProvider;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;
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
        Stream stream = exchng.getContext().getTypeConverter().mandatoryConvertTo(Stream.class, o);
        JsonHandler.getInstance().marshalStreamToJsonArray(stream, out);
    }

    /**
     * For correct deserialization of values that are not primitive JSON types the Metadata must be
     * present as a header with the name of the {@link JsonHandler.ATTR_DATASET_METADATA} constant.
     *
     * @param exchange
     * @param in
     * @return
     * @throws Exception
     */
    @Override
    public Object unmarshal(Exchange exchange, InputStream in) throws Exception {
        DatasetMetadata meta = exchange.getIn().getHeader(JsonHandler.ATTR_DATASET_METADATA, DatasetMetadata.class);
        if (meta == null) {
            meta = new DatasetMetadata(MoleculeObject.class);
        }
        return JsonHandler.getInstance().unmarshalDataset(meta, in);
    }

}
