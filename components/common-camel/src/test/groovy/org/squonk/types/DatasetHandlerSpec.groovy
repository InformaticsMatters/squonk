package org.squonk.types

import org.squonk.dataset.Dataset
import spock.lang.Specification

class DatasetHandlerSpec extends Specification {

    void "instantiate"() {

        when:
        def data = new FileInputStream("../../data/testfiles/Kinase_inhibs.json.gz")
        def meta = new FileInputStream("../../data/testfiles/Kinase_inhibs.metadata")
        def handler = new DatasetHandler(MoleculeObject.class)
        Dataset value = handler.create(['data': data, 'metadata': meta])

        then:
        value != null
        value instanceof Dataset
        value.getType() == MoleculeObject.class
        value.items.size() == 36

        cleanup:
        data.close()
        meta.close()

    }
}
