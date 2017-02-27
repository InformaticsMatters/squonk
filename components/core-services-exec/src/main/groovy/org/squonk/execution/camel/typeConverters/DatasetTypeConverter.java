package org.squonk.execution.camel.typeConverters;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.squonk.core.client.StructureIOClient;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.MoleculeObject;
import org.squonk.types.SDFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 26/02/17.
 */
@Converter
public class DatasetTypeConverter {

    @Converter
    public static SDFile convertDatasetToSDFile(Dataset<MoleculeObject> dataset, Exchange exchange) throws IOException {
        if (dataset.getType() == MoleculeObject.class) {
            return convertMoleculeObjectDatasetToSDFile(new MoleculeObjectDataset(dataset), exchange);
        } else {
            return null;
        }
    }

    @Converter
    public static SDFile convertMoleculeObjectDatasetToSDFile(MoleculeObjectDataset dataset, Exchange exchange) throws IOException {
        InputStream is = StructureIOClient.CDK.datasetExportToSdf(dataset.getDataset(), false);
        SDFile sdf = new SDFile(is);
        return sdf;
    }
}
