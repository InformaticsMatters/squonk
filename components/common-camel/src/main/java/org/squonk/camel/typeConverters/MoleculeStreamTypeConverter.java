/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

package org.squonk.camel.typeConverters;

import org.apache.camel.TypeConverters;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.reader.SDFReader;
import org.squonk.types.SDFile;
import org.squonk.types.io.JsonHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author timbo
 */
@Converter
public class MoleculeStreamTypeConverter implements TypeConverters {


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
    public static DatasetMetadata convertJsonToDatasetMetadata(String json, Exchange exchange) throws IOException {
        return JsonHandler.getInstance().objectFromJson(json, DatasetMetadata.class);
    }

//    @Converter
//    public static MoleculeObject convertJsonToMoleculeObject(String json, Exchange exchange) throws IOException {
//        System.out.println("==================================================================");
//        MoleculeObject mo = null;
//        try {
//            mo = JsonHandler.getInstance().objectFromJson(json, MoleculeObject.class);
//        } catch (JsonParseException e) {
//            //fail
//        }
//        return mo;
//    }



}
