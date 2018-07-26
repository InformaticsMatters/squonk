package org.squonk.types

import org.squonk.dataset.Dataset
import org.squonk.io.FileDataSource
import spock.lang.Specification

class DatasetHandlerSpec extends Specification {

    void "instantiate"() {

        when:
        def data = new FileDataSource(null, null, new File("../../data/testfiles/Kinase_inhibs.json.gz"), true)
        def meta = new FileDataSource(null, null, new File("../../data/testfiles/Kinase_inhibs.metadata"), false)
        def handler = new DatasetHandler(MoleculeObject.class)
        Dataset value = handler.create(['data': data, 'metadata': meta])

        then:
        value != null
        value instanceof Dataset
        value.getType() == MoleculeObject.class
        value.items.size() == 36

    }
}
