package org.squonk.openchemlib.molecule

import com.actelion.research.chem.SmilesParser
import com.actelion.research.chem.StereoMolecule
import org.squonk.data.Molecules
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream
import java.util.zip.GZIPInputStream

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


    void "read tdt stream"() {

        Stream mols = Molecules.nci1000Molecules().stream().parallel()

        when:
        AtomicInteger total = new AtomicInteger(0)
        AtomicInteger fails = new AtomicInteger(0)
        long count = mols.map() { mo ->
            int lTot = total.incrementAndGet()
            StereoMolecule mol = new StereoMolecule();
            try {
                mol = OCLMoleculeUtils.importSmiles(mo.source)
                String idCode = mol.IDCode
                //println "$smiles -> $idCode"
            } catch (Exception e) {
                fails.incrementAndGet()
                //println "$smiles -> $idCode"
                println e.getMessage()
            }
            return mol
        }.count()
        println "count=$count total=$total fails=$fails"

        then:
        total.get() == 1000
        fails.get() == 0

        cleanup:
        mols?.close()
    }
}
