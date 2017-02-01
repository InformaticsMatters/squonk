package org.squonk.io

import org.squonk.dataset.Dataset
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import org.squonk.types.SDFile
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

/**
 * Created by timbo on 02/01/17.
 */
class IODescriptorsSpec extends Specification {

    void "basic support molecule but not vice versa"() {
        when:
        def basic = new IODescriptor("in1", CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON, Dataset.class, BasicObject)
        def molecule = new IODescriptor("in2", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject)

        then:
        IODescriptors.supports(basic, molecule)
        !IODescriptors.supports(molecule, basic)
    }

    void "dataset not supports sdf"() {
        when:
        def basic = new IODescriptor("in1", CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON, Dataset.class, BasicObject)
        def sdf = new IODescriptor("in2", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class, null)

        then:
        !IODescriptors.supports(basic, sdf)
        !IODescriptors.supports(sdf, basic)
    }

}
