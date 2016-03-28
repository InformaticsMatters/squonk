package org.squonk.camel.typeConverters;

import com.im.lac.types.BasicObject;
import com.im.lac.types.MoleculeObject;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.reader.SDFReader;
import org.squonk.types.SDFile;
import org.squonk.types.io.JsonHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author timbo
 */
@Converter
public class MoleculeStreamTypeConverter {

    @Converter
    public static Stream<MoleculeObject> convertMoleculeObjectDatasetToStream(MoleculeObjectDataset mods, Exchange exchange) throws IOException {
        return mods.getDataset().getStream();
    }

    @Converter
    public static List<MoleculeObject> convertMoleculeObjectDatasetToList(MoleculeObjectDataset mods, Exchange exchange) throws IOException {
        return mods.getDataset().getItems();
    }

    @Converter
    public static <T extends BasicObject> Stream<T> convertDatasetToStream(Dataset<T> ds, Exchange exchange) throws IOException {
        return ds.getStream();
    }

    @Converter
    public static Dataset<MoleculeObject> convertMoleculeObjectDatasetToDataset(MoleculeObjectDataset mods, Exchange exchange) throws IOException {
        return mods.getDataset();
    }

    /** This is deliberately not registered as a TypeConverter
     *
     * @param dataset
     * @param exchange
     * @return
     * @throws IOException
     */
    public static MoleculeObjectDataset convertMoleculeObjectDatasetToDataset(Dataset dataset, Exchange exchange) throws IOException {
        if (dataset.getType() == MoleculeObject.class) {
            return new MoleculeObjectDataset(dataset);
        } else {
            return null;
        }
    }

    @Converter
    public static Stream<MoleculeObject> convertSDFileToMoleculeObjectSteam(SDFile sdf, Exchange exchange) throws IOException {
        SDFReader reader = new SDFReader(sdf.getInputStream());
        if (exchange != null) {
            String nfn = exchange.getIn().getHeader(SDFile.PROP_NAME_FIELD_NAME, String.class);
            if (nfn != null && nfn.length() > 0) {
                reader.setNameFieldName(nfn);
            }
        }
        Stream<MoleculeObject> mols = reader.asStream();
        return mols;
    }

    @Converter
    public static Dataset<MoleculeObject> convertSDFileToDataset(SDFile sdf, Exchange exchange) throws IOException {
        Stream<MoleculeObject> mols = convertSDFileToMoleculeObjectSteam(sdf, exchange);
        Dataset dataset = new Dataset(MoleculeObject.class, mols);
        return dataset;
    }

    @Converter
    public static MoleculeObjectDataset convertSDFileToMoleculeObjectDataset(SDFile sdf, Exchange exchange) throws IOException {
        Stream<MoleculeObject> mols = convertSDFileToMoleculeObjectSteam(sdf, exchange);
        MoleculeObjectDataset dataset = new MoleculeObjectDataset(mols);
        return dataset;
    }

    @Converter
    public static MoleculeObjectDataset convertInputStreamToMoleculeObjectDataset(InputStream is, Exchange exchange) throws IOException {
        DatasetMetadata meta = exchange.getIn().getHeader(CamelCommonConstants.HEADER_METADATA, DatasetMetadata.class);
        Dataset<MoleculeObject> dataset = new Dataset<>(MoleculeObject.class, is, meta);
        return new MoleculeObjectDataset(dataset);
    }

    @Converter
    public static SDFile convertInputStreamToSDFile(InputStream is, Exchange exchange) throws IOException {
        return new SDFile(is);
    }

    @Converter
    public static DatasetMetadata convertJsonToDatasetMetadata(String json, Exchange exchange) throws IOException {
        return JsonHandler.getInstance().objectFromJson(json, DatasetMetadata.class);
    }


}
