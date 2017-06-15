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

package org.squonk.camel.cdk.typeConverters;

import org.squonk.types.MoleculeObject;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.cdk.io.CDKMoleculeIOUtils;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.CDKSDFile;

/**
 * Created by timbo on 27/03/2016.
 */
@Converter
public class CDKTypeConverter {

    @Converter
    public static CDKSDFile convertDatasetToSDFile(Dataset<MoleculeObject> mols, Exchange exch) throws Exception {
        boolean haltOnError = (exch == null ? true : exch.getIn().getHeader(CamelCommonConstants.HEADER_HALT_ON_ERROR, Boolean.TRUE, Boolean.class));
        return CDKMoleculeIOUtils.covertToSDFile(mols.getStream(), haltOnError);
    }


    @Converter
    public static CDKSDFile convertDatasetToSDFile(MoleculeObjectDataset mols, Exchange exch) throws Exception {
        boolean haltOnError = (exch == null ? true : exch.getIn().getHeader(CamelCommonConstants.HEADER_HALT_ON_ERROR, Boolean.TRUE, Boolean.class));
        return CDKMoleculeIOUtils.covertToSDFile(mols.getStream(), haltOnError);
    }

}
