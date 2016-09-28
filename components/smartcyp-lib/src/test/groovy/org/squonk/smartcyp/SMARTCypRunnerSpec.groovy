package org.squonk.smartcyp

import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 * Created by timbo on 28/09/2016.
 */
class SMARTCypRunnerSpec extends Specification {

    void "execute smiles 10"() {
        Dataset dataset = Molecules.nci10Dataset()
        SMARTCypRunner runner = new SMARTCypRunner()

        when:
        def results = runner.execute(dataset)
        def items = results.items
        items.eachWithIndex { v, i ->
            println "$i $v.source"
            println v.values
        }

        then:
        items.size() == 10
    }


    void "execute molfile"() {
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        SMARTCypRunner runner = new SMARTCypRunner()

        when:
        def results = runner.execute(dataset)
        def items = results.items
        items.eachWithIndex { v, i ->
            println "$i $v.source"
            println v.values
        }

        then:
        items.size() == 36
    }

    void fragments() {

        def mols = [
                new MoleculeObject('CC1=CC(=O)C=CC1=O', 'smiles'), new MoleculeObject('C.CC1=CC(=O)C=CC1=O', 'smiles')
        ]
        def dataset = new Dataset(MoleculeObject.class, mols)

        SMARTCypRunner runner = new SMARTCypRunner()

        when:
        def results = runner.execute(dataset)
        def items = results.items
        items.eachWithIndex { v, i ->
            println "$i $v.source"
            println v.values
        }

        then:
        items.size() == 2


    }

}
