/*
 * Copyright (c) 2018 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.camel.processor

import org.squonk.types.MoleculeObject
import org.squonk.util.CommonConstants
import org.squonk.util.MpoFunctions
import spock.lang.Specification

import static org.squonk.camel.processor.MpoAccumulatorProcessor.AccumulatorStrategy.*;

class MpoAccumulatorProcessorSpec extends Specification {

    private static final String PROP_NAME = "name"

    static MpoAccumulatorProcessor createProcessor(MpoAccumulatorProcessor.AccumulatorStrategy mode, boolean allowNullValues) {
        return new MpoAccumulatorProcessor(PROP_NAME, "desc", Float.class, "", "", mode, allowNullValues)
                .addHumpFunction("A", MpoFunctions.createRampFunction(0d, 1d, 3d, 5d)) // -> 0.5
                .addHumpFunction("B", MpoFunctions.createRampFunction(0d, 1d, 3d, 5d)) // -> 0
                .addHumpFunction("C", MpoFunctions.createRampFunction(0d, 1d, 3d, 5d)) // -> 0
                .addHumpFunction("D", MpoFunctions.createRampFunction(0d, 1d, 3d, 5d)) // -> 1
    }

    void "test sum"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(SUM, false)

        when:
        p.processMoleculeObject(mo)

        then:
        mo.getValue(PROP_NAME) == 1.5d
    }

    void "test mean"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(MEAN, false)

        when:
        p.processMoleculeObject(mo)

        then:
        mo.getValue(PROP_NAME) == 1.5d / 4d

    }

    void "test with null not allowed"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)


        def p = createProcessor(SUM, false)
        when:
        p.processMoleculeObject(mo)

        then:
        mo.getValue(PROP_NAME) == null
    }

    void "test with null allowed"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)


        def p = createProcessor(SUM, true)
        when:
        p.processMoleculeObject(mo)

        then:
        mo.getValue(PROP_NAME) == 0.5d
    }

    void "test filter all out of range"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(SUM, false)

        when:
        p.processMoleculeObject(mo)
        def result = p.filter(mo, CommonConstants.VALUE_INCLUDE_ALL, 0d, 1d)

        then:
        result == true
        mo.getValue(PROP_NAME) == 1.5d
    }

    void "test filter pass in range"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(SUM, false)

        when:
        p.processMoleculeObject(mo)
        def result = p.filter(mo, CommonConstants.VALUE_INCLUDE_PASS, 1d, 2d)

        then:
        result == true
        mo.getValue(PROP_NAME) == 1.5d
    }

    void "test filter pass out of range"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(SUM, false)

        when:
        p.processMoleculeObject(mo)
        def result = p.filter(mo, CommonConstants.VALUE_INCLUDE_PASS, 0d, 1d)

        then:
        result == false
    }

    void "test filter fail in range"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(SUM, false)

        when:
        p.processMoleculeObject(mo)
        def result = p.filter(mo, CommonConstants.VALUE_INCLUDE_FAIL, 1d, 2d)

        then:
        result == false
    }

    void "test filter fail in range min only"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(SUM, false)

        when:
        p.processMoleculeObject(mo)
        def result = p.filter(mo, CommonConstants.VALUE_INCLUDE_FAIL, 1d, null)

        then:
        result == false
    }

    void "test filter fail in range max only"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(SUM, false)

        when:
        p.processMoleculeObject(mo)
        def result = p.filter(mo, CommonConstants.VALUE_INCLUDE_FAIL, null, 2d)

        then:
        result == false
    }

    void "test filter fail out of range"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(SUM, false)

        when:
        p.processMoleculeObject(mo)
        def result = p.filter(mo, CommonConstants.VALUE_INCLUDE_FAIL, 0d, 1d)

        then:
        result == true
        mo.getValue(PROP_NAME) == 1.5d
    }

    void "test filter fail out of range max only"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(SUM, false)

        when:
        p.processMoleculeObject(mo)
        def result = p.filter(mo, CommonConstants.VALUE_INCLUDE_FAIL, null, 1d)

        then:
        result == true
        mo.getValue(PROP_NAME) == 1.5d
    }

    void "test filter fail out of range min only"() {
        def mo = new MoleculeObject("smiles", "smiles")
        mo.putValue("A", 4d)
        mo.putValue("B", 1d)
        mo.putValue("C", 3d)
        mo.putValue("D", 6d)

        def p = createProcessor(SUM, false)

        when:
        p.processMoleculeObject(mo)
        def result = p.filter(mo, CommonConstants.VALUE_INCLUDE_FAIL, 3d, null)

        then:
        result == true
        mo.getValue(PROP_NAME) == 1.5d
    }

}
