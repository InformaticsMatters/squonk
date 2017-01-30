package org.squonk.io;

import org.squonk.dataset.Dataset;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.types.SDFile;

import static org.squonk.util.CommonMimeTypes.*;

/**
 * Created by timbo on 27/12/16.
 */
public class IODescriptors {

    public static IODescriptor createMoleculeObjectDataset(String name) {
        return new IODescriptor(name, MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class);
    }

    public static IODescriptor[] createMoleculeObjectDatasetArray(String name) {
        return new IODescriptor[] {createMoleculeObjectDataset(name)};
    }

    public static IODescriptor createBasicObjectDataset(String name) {
        return new IODescriptor(name, MIME_TYPE_DATASET_BASIC_JSON, Dataset.class, BasicObject.class);
    }

    public static IODescriptor[] createBasicObjectDatasetArray(String name) {
        return new IODescriptor[] {createBasicObjectDataset(name)};
    }

    public static IODescriptor createSDF(String name) {
        return new IODescriptor(name, MIME_TYPE_MDL_SDF, SDFile.class, null);
    }

    public static IODescriptor[] createSDFArray(String name) {
        return new IODescriptor[] {createSDF(name)};
    }

    public static String buildDisplayName(IODescriptor iod) {
        if (iod.getSecondaryType() == null) {
            return iod.getName() + "[" + iod.getPrimaryType().getSimpleName() + "]";
        } else {
            return iod.getName() + "[" + iod.getPrimaryType().getSimpleName() + ":" + iod.getSecondaryType().getSimpleName() + "]";
        }
    }
}
