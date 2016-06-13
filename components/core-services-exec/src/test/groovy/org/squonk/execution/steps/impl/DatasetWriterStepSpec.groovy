package org.squonk.execution.steps.impl

import com.im.lac.types.MoleculeObject
import org.squonk.dataset.Dataset

import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetWriterStepSpec extends Specification {

    void "test write mols"() {

        def mols = [
                new MoleculeObject("C", "smiles"),
                new MoleculeObject("CC", "smiles"),
                new MoleculeObject("CCC", "smiles")
        ]
        Dataset ds = new Dataset(MoleculeObject.class, mols)

        VariableManager varman = new VariableManager(null, 1, 1);
        DatasetWriterStep step = new DatasetWriterStep()
        Long producer = 1
        step.configure(producer, "job1",
                [:],
                [(DatasetWriterStep.VAR_INPUT_DATASET): new VariableKey(producer, "input")],
                [:])
        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                ds)

        when:
        step.execute(varman, null)
        Dataset dataset = varman.getValue(new VariableKey(producer, DatasetWriterStep.VAR_OUTPUT_DATASET), Dataset.class)

        then:
        dataset != null

    }

}