package org.squonk.camel.dataformat;

import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.io.JsonHandler;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 *
 * @author timbo
 */
public class MoleculeObjectDatasetJsonDataFormat implements DataFormat {

    private final static Logger LOG = Logger.getLogger(MoleculeObjectDatasetJsonDataFormat.class.getName());

    /**
     * Writes a {@link Dataset} of @{link
     * com.im.lac.types.MoleculeObject}s or a
     * {@link MoleculeObjectDataset} to the OutputStream.
     * Generates the {@link DatasetMetadata} as it does so,
     * and then sets that DatasetMetadata as a header named by the value of the
     * {@link JsonHandler.ATTR_DATASET_METADATA} constant.
     *
     * @param exch
     * @param o
     * @param out
     * @throws Exception
     */
    @Override
    public void marshal(Exchange exch, Object o, OutputStream out) throws Exception {
        LOG.log(Level.FINER, "Marshaling {0}", o);
        Dataset ds = exch.getContext().getTypeConverter().convertTo(Dataset.class, o);
        if (ds == null) {
            MoleculeObjectDataset mods = exch.getContext().getTypeConverter().convertTo(MoleculeObjectDataset.class, o);
            if (mods == null) {
                throw new IllegalStateException("No Dataset of MoleculeObjects found");
            }
            ds = mods.getDataset();
        } else if (ds.getType() != MoleculeObject.class) {
            throw new IllegalStateException("Dataset is not of type MoleculeObject");
        }

        Dataset.DatasetMetadataGenerator generator = ds.createDatasetMetadataGenerator();
        try (Stream s = generator.getAsStream()) {
            JsonHandler.getInstance().marshalStreamToJsonArray(s, out);
        }
        // finally wait for the MD generation to complete
        DatasetMetadata md = generator.getDatasetMetadata();
        exch.getOut().setHeader(JsonHandler.ATTR_DATASET_METADATA, md);
    }

    /**
     * Reads the InputStream as objects into a new
     * {@link Dataset}. The DatasetMetadata MUST be defined
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
            meta = new DatasetMetadata(MoleculeObject.class);
            LOG.log(Level.INFO, "DatasetMetadata not found as header named {0}. Complex value types will not be handled correctly.", JsonHandler.ATTR_DATASET_METADATA);
        }
        Dataset ds = JsonHandler.getInstance().unmarshalDataset(meta, in);
        return new MoleculeObjectDataset(ds);
    }

}
