package org.squonk.dataset;

import java.util.List;
import java.util.Map;

/**
 * Created by timbo on 08/03/16.
 */
public class DatasetUtils {

    /**
     * Merges multiple metadatas. The value class mappings and properties are combined with later values replacing earlier ones.
     * The size is retained if all sizes are the same, otherwise size is set to zero.
     * Dataset and field properties are merged according to the behavior of the corresponding mergeDatasetProperty() and
     * mergeFieldProperty() methods of DatasetMetadata.
     *
     * @param meta
     * @return
     * @throws IncompatibleMetadataException If metadatas are of different types.
     */
    public static DatasetMetadata mergeDatasetMetadata(DatasetMetadata... meta) throws IncompatibleMetadataException {

        if (meta == null || meta.length == 0) {
            return null;
        } else if (meta.length == 1) {
            return meta[0];
        } else {
            DatasetMetadata result = new DatasetMetadata(meta[0].getType(), null, -1);
            for (int i=0; i<meta.length; i++) {
                if (meta[i].getType() != result.getType()) {
                    throw new IncompatibleMetadataException("Can't merge metadata of different types");
                }
                result.getValueClassMappings().putAll(meta[i].getValueClassMappings());
                if (result.getSize() != meta[i].getSize()) {
                    // sizes are inconsistent to set to zero to say that we don't know
                    result.setSize(0);
                }

                // merge dataset props
                Map<String,Object> datasetProps = meta[i].getProperties();
                for (Map.Entry<String,Object> e: datasetProps.entrySet()) {
                    result.mergeDatasetProperty(e.getKey(), e.getValue());
                }

                // merge field props
                List<DatasetMetadata.PropertiesHolder> phs = meta[i].getFieldMetaProps();
                for (DatasetMetadata.PropertiesHolder ph : phs) {
                    for (Map.Entry<String,Object> e: ph.getValues().entrySet()) {
                        result.mergeFieldProperty(ph.getFieldName(), e.getKey(), e.getValue());
                    }
                }
            }


            return result;
        }
    }
}
