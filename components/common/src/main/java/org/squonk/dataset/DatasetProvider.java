package org.squonk.dataset;

import org.squonk.types.BasicObject;

/**
 *
 * @author timbo
 */
public interface DatasetProvider<T extends BasicObject> {
    
    Dataset<T> getDataset() throws Exception;

    /** Get just the metadata without opening any additional streams for the data.
     *
     * @return
     * @throws Exception
     */
    DatasetMetadata<T> getMetadata() throws Exception;
    
}
