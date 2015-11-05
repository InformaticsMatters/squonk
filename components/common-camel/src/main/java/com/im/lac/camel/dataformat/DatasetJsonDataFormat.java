package com.im.lac.camel.dataformat;

import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 *
 * @author timbo
 */
public class DatasetJsonDataFormat implements DataFormat {

    /**
     * Writes the {@link com.squonk.dataset.Dataset} items to the OutputStream.
     * Generates the {@link com.squonk.dataset.DatasetMetadata} as it does so,
     * and then sets the DatasetMetadata as a header named by the value of the
     * {@link JsonHandler.ATTR_DATASET_METADATA} constant.
     *
     * @param exch
     * @param o
     * @param out
     * @throws Exception
     */
    @Override
    public void marshal(Exchange exch, Object o, OutputStream out) throws Exception {
        Dataset ds = exch.getContext().getTypeConverter().mandatoryConvertTo(Dataset.class, o);
        Dataset.DatasetMetadataGenerator generator = ds.createDatasetMetadataGenerator();
        try (Stream s = generator.getAsStream()) {
            JsonHandler.getInstance().marshalStreamToJsonArray(s, out);
        }
        // finally wait for the MD generation to complete
        DatasetMetadata md = generator.getDatasetMetadata();

        exch.getIn().setHeader(JsonHandler.ATTR_DATASET_METADATA, md);
    }

    /**
     * Reads the InputStream as objects into a new
     * {@link com.squonk.dataset.Dataset}. The DatasetMetadata MUST be defined
     * as a header named by the value of the
     * {@link JsonHandler.ATTR_DATASET_METADATA} constant.
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
            throw new IllegalStateException("No DatasetMetadata present as header named " + JsonHandler.ATTR_DATASET_METADATA);
        }
        return JsonHandler.getInstance().unmarshalDataset(meta, in);
    }

}
