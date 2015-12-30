package org.squonk.camel.dataformat;

import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.io.JsonHandler;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 * Camel DataFormat for handling a Stream&lt;MoleculeObject&gt;.
 *
 *
 * @author timbo
 */
public class MoleculeObjectJsonDataFormat implements DataFormat {

    private final static Logger LOG = Logger.getLogger(MoleculeObjectJsonDataFormat.class.getName());

    @Override
    public void marshal(Exchange exch, Object o, OutputStream out) throws Exception {
        Stream<MoleculeObject> stream = exch.getContext().getTypeConverter().mandatoryConvertTo(Stream.class, o);
        JsonHandler.getInstance().marshalStreamToJsonArray(stream, out);
    }

    /**
     * For correct deserialization of values that are not primitive JSON types the Metadata must be
     * present as a header with the name of the {@link JsonHandler.ATTR_DATASET_METADATA} constant.
     *
     * @param exch
     * @param in
     * @return
     * @throws Exception
     */
    @Override
    public Object unmarshal(Exchange exch, InputStream in) throws Exception {
        DatasetMetadata meta = exch.getIn().getHeader(JsonHandler.ATTR_DATASET_METADATA, DatasetMetadata.class);
        if (meta == null) {
            meta = new DatasetMetadata(MoleculeObject.class);
            LOG.log(Level.INFO, "DatasetMetadata not found as header named {0}. Complex value types will not be handled correctly.", JsonHandler.ATTR_DATASET_METADATA);
        }
        Dataset<MoleculeObject> ds = JsonHandler.getInstance().unmarshalDataset(meta, in);
        return ds.getStream();
    }

}
