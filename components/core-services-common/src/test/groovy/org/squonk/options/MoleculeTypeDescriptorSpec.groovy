package org.squonk.options

import org.squonk.options.types.Structure
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 15/01/16.
 */
class MoleculeTypeDescriptorSpec extends Specification {

    void "test json"() {

        MoleculeTypeDescriptor discrete = new MoleculeTypeDescriptor(MoleculeTypeDescriptor.MoleculeType.DISCRETE, ["smiles"] as String[]);

        when:
        String json = JsonHandler.getInstance().objectToJson(discrete)
        MoleculeTypeDescriptor td = JsonHandler.getInstance().objectFromJson(json, TypeDescriptor.class)

        then:
        println json
        json != null
        td != null
        td.type == Structure.class
        td.formats.length == 1
        td.formats[0] == "smiles"

    }
}
