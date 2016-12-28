package org.squonk.io;

import org.squonk.dataset.Dataset;
import org.squonk.types.MoleculeObject;
import org.squonk.types.SDFile;

import static org.squonk.util.CommonMimeTypes.*;

/**
 * Created by timbo on 27/12/16.
 */
public class IODescriptors {

    public static IODescriptor createMoleculeObjectDataset(String name, IORoute route) {
        return new IODescriptor(name, MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class, IOMultiplicity.STREAM, route);
    }

    public static IODescriptor createSDF(String name, IORoute route) {
        return new IODescriptor(name, MIME_TYPE_MDL_SDF, SDFile.class, null, IOMultiplicity.ITEM, route);
    }
}
