package org.squonk.io;

import org.squonk.dataset.Dataset;
import org.squonk.types.BasicObject;
import org.squonk.types.CSVFile;
import org.squonk.types.MoleculeObject;
import org.squonk.types.SDFile;

import static org.squonk.util.CommonMimeTypes.*;

/**
 * Created by timbo on 27/12/16.
 */
public class IODescriptors {


    public static IODescriptor createString(String name) {
        return new IODescriptor(name, MIME_TYPE_TEXT_PLAIN, String.class, null);
    }

    public static IODescriptor[] createStringArray(String name) {
        return new IODescriptor[] {createString(name)};
    }

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

    public static IODescriptor createCSV(String name) {
        return new IODescriptor(name, MIME_TYPE_TEXT_CSV, CSVFile.class, null);
    }

    public static IODescriptor[] createCSVArray(String name) {
        return new IODescriptor[] {createCSV(name)};
    }

    public static String buildDisplayName(IODescriptor iod) {
        if (iod.getSecondaryType() == null) {
            return iod.getName() + "[" + iod.getPrimaryType().getSimpleName() + "]";
        } else {
            return iod.getName() + "[" + iod.getPrimaryType().getSimpleName() + ":" + iod.getSecondaryType().getSimpleName() + "]";
        }
    }

    /** Does the first descriptor support the data of the type of the second
     *
     * @param first
     * @param second
     * @return
     */
    public static boolean supports(IODescriptor first, IODescriptor second) {
       return supports(first.getPrimaryType(), first.getSecondaryType(), second.getPrimaryType(), second.getSecondaryType());
    }

    public static boolean supports(Class primary1, Class secondary1, Class primary2, Class secondary2) {
        if (primary1 != primary2 && !primary2.isAssignableFrom(primary1)) {
            return false;
        }

        if (secondary1 == secondary2 || secondary1.isAssignableFrom(secondary2)) {
            return true;
        }
        return false;
    }


    public static boolean isSameType(IODescriptor iod, Class primary, Class secondary) {
        return iod.getPrimaryType() == primary && iod.getSecondaryType() == secondary;
    }

    public static boolean isSameType(IODescriptor iod, String mediaType) {
        return iod.getMediaType().equals(mediaType);
    }

}