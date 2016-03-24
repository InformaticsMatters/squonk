package org.squonk.camel.typeConverters

import org.squonk.dataset.Dataset
import org.squonk.types.SDFile
import spock.lang.Specification

/**
 * Created by timbo on 24/03/2016.
 */
class MoleculeStreamTypeConverterSpec extends Specification {


    void "sdf to dataset"() {

        InputStream is = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        SDFile sdf = new SDFile(is)

        when:
        Dataset ds = MoleculeStreamTypeConverter.convertSDFileToMoleculeObjectDataset(sdf, null)

        then:
        ds != null
        ds.items.size() == 36

        cleanup:
        is.close()

    }

}
