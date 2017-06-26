/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.io;

import org.squonk.dataset.Dataset;
import org.squonk.types.*;

import static org.squonk.util.CommonMimeTypes.*;
import static org.squonk.util.CommonMimeTypes.MIME_TYPE_TRIPOS_MOL2;

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

    public static IODescriptor createMoleculeObject(String name) {
        return new IODescriptor(name, MIME_TYPE_MOLECULE_OBJECT_JSON, MoleculeObject.class);
    }

    public static IODescriptor createMolfile(String name) {
        return new IODescriptor(name, MIME_TYPE_MDL_MOLFILE, MolFile.class);
    }

    public static IODescriptor createMol2(String name) {
        return new IODescriptor(name, MIME_TYPE_TRIPOS_MOL2, Mol2File.class);
    }

    public static IODescriptor createPDB(String name) {
        return new IODescriptor(name, MIME_TYPE_PDB, PDBFile.class);
    }

    public static IODescriptor createZipFile(String name) {
        return new IODescriptor(name, MIME_TYPE_ZIP_FILE, ZipFile.class);
    }


    public static IODescriptor[] createMoleculeObjectArray(String name) {
        return new IODescriptor[] {createMoleculeObjectDataset(name)};
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
