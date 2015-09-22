package com.im.lac.camel.dataformat;

import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.dataset.MoleculeObjectDataset;
import com.squonk.types.io.JsonHandler;
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
     * Writes a {@link com.squonk.dataset.Dataset} of @{link com.im.lac.types.MoleculeObject}s or a
     * {@link com.squonk.dataset.MoleculeObjectDataset} to the OutputStream. Generates the
     * {@link com.squonk.dataset.DatasetMetadata} as it does so, and then sets that DatasetMetadata
     * as a header named by the value of the {@link JsonHandler.ATTR_DATASET_METADATA} constant.
     *
     * @param exch
     * @param o
     * @param out
     * @throws Exception
     */
    @Override
    public void marshal(Exchange exch, Object o, OutputStream out) throws Exception {
        LOG.finer("Marshaling " + o);
        Dataset ds = exch.getContext().getTypeConverter().convertTo(Dataset.class, o);
        if (ds == null) {
            MoleculeObjectDataset mods = exch.getContext().getTypeConverter().convertTo(MoleculeObjectDataset.class, o);
            if (mods == null) {
                throw new IllegalStateException("No Dataset of MoleculeObjects found");
            }
            ds = mods.getDataset();
        } else {
            if (ds.getType() != MoleculeObject.class) {
                throw new IllegalStateException("Dataset is not of type MoleculeObject");
            }
        }
        Stream s = ds.createMetadataGeneratingStream(ds.getStream());
        JsonHandler.getInstance().marshalStreamToJsonArray(s, out);

        exch.getOut().setHeader(JsonHandler.ATTR_DATASET_METADATA, ds.getMetadata());
    }

    /**
     * Reads the InputStream as objects into a new {@link com.squonk.dataset.Dataset}. The
     * DatasetMetadata MUST be defined as a header named by the value of the
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
