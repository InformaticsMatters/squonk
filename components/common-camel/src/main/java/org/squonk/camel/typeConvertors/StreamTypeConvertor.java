package org.squonk.camel.typeConvertors;

import com.im.lac.types.BasicObject;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.StreamProvider;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.SDFile;
import org.squonk.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
@Converter
public class StreamTypeConvertor {

    @Converter
    public static Iterator convertStreamToIterator(Stream s, Exchange exchange) {
        return s.iterator();
    }
    
    @Converter
    public static <T> Iterator<T> convertStreamProviderToIterator(StreamProvider<T> sp, Exchange exchange) throws IOException {
        return sp.getStream().iterator();
    }
    
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
    
    @Converter
    public static SDFile convertInputStreamToSDFile(InputStream is, Exchange exchange) throws IOException {
        // TODO - we gunzip it if necessary because the ChemAxon MolImporter doesn't handle telling you the format if its gzipped
        // if/when we switch to a different parser this might not be necessary
        return new SDFile(IOUtils.getGunzippedInputStream(is));
    }
    
    
}
