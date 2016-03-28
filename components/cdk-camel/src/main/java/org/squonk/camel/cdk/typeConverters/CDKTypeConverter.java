package org.squonk.camel.cdk.typeConverters;

import com.im.lac.types.MoleculeObject;
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
