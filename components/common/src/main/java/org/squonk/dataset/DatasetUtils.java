package org.squonk.dataset;

/**
 * Created by timbo on 08/03/16.
 */
public class DatasetUtils {

    /**
     * Merges multiple metadatas. The value class mappings and properties are combined with later values replacign earlier ones.
     * The size is retained if all sizes are the same, otherwise size is set to zero.
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
            DatasetMetadata result = new DatasetMetadata(meta[0].getType(), meta[0].getValueClassMappings(), meta[0].getSize(), meta[0].getProperties());
            for (int i=1; i<meta.length; i++) {
                if (meta[i].getType() != result.getType()) {
                    throw new IncompatibleMetadataException("Can't merge metadata of different types");
                }
                result.getValueClassMappings().putAll(meta[i].getValueClassMappings());
                result.getProperties().putAll(meta[i].getProperties());
                if (result.getSize() != meta[i].getSize()) {
                    // sizes are inconsistent to set to zero to say that we don't know
                    result.setSize(0);
                }
            }
            return result;
        }
    }
}
