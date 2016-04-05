package org.squonk.openchemlib.molecule

import com.actelion.research.chem.StereoMolecule
import org.squonk.data.Molecules
import spock.lang.Specification

/**
 * Created by timbo on 05/04/16.
 */
class OCLMoleculeUtilsSpec extends Specification {

    void "parse smiles"() {

        when:
        StereoMolecule mol = OCLMoleculeUtils.importSmiles(Molecules.ethanol.smiles)

        then:
        mol != null
    }

    void "parse molfile"() {

        when:
        StereoMolecule mol = OCLMoleculeUtils.importMolfile(Molecules.ethanol.v2000)

        then:
        mol != null
    }

    void "guess format"() {

        when:
        StereoMolecule mol1 = OCLMoleculeUtils.importString(Molecules.ethanol.smiles, null)
        StereoMolecule mol2 = OCLMoleculeUtils.importString(Molecules.ethanol.v2000, null)

        then:
        mol1 != null
        mol2 != null
    }
}
