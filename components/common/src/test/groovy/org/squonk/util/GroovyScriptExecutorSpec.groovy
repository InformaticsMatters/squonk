/*
 * Copyright (c) 2017 Informatics Matters Ltd.
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

package org.squonk.util

import org.squonk.types.MoleculeObject
import org.squonk.dataset.Dataset
import spock.lang.Specification

import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * Created by timbo on 28/12/15.
 */
class GroovyScriptExecutorSpec extends Specification {

    void "test run groovy"() {

        String script1 = """import org.squonk.dataset.transform.TransformDefinitions
new TransformDefinitions()
        .deleteField("C")
        .renameField("B", "M")
        .convertField("A", Integer.class)"""

        String script2 = '''
import org.squonk.types.MoleculeObject
new MoleculeObject("C", "smiles")'''

        ScriptEngineManager manager = new ScriptEngineManager(this.getClass().getClassLoader());
        ScriptEngine engine = manager.getEngineByName("Groovy");

        Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);

        when:
        Object result = engine.eval(script1, bindings);

        then:
        result != null
        println result
    }

    void "test simple execute"() {

        ScriptEngine engine = GroovyScriptExecutor.createScriptEngine(this.getClass().getClassLoader())

        when:
        Integer i = GroovyScriptExecutor.executeAndReturnValue(Integer.class, engine, "new Integer(999)", null)

        then:
        i == 999

    }

    void "test with import"() {

        ScriptEngine engine = GroovyScriptExecutor.createScriptEngine(this.getClass().getClassLoader())
        String script = GroovyScriptExecutor.addImportsToScript("new MoleculeObject('CCC', 'smiles')",
                 [MoleculeObject.class.getName()] as String[])

        when:
        MoleculeObject mo = GroovyScriptExecutor.executeAndReturnValue(Integer.class, engine, script, null);

        then:
        mo != null

    }

    void "basic script"() {

        String script = '''
import org.squonk.dataset.Dataset
import org.squonk.types.MoleculeObject

println "input: $input ${input.metadata}"
def s = input.stream.peek {
    it.values['hero'] ='superman'
}
return new Dataset(input.metadata.type, s)'''

        List<MoleculeObject> mols = new ArrayList<>()
        mols.add(new MoleculeObject("C", "smiles", Collections.singletonMap("X", 1.1)))
        mols.add(new MoleculeObject("CC", "smiles", Collections.singletonMap("X", 2.2)))
        mols.add(new MoleculeObject("CCC", "smiles", Collections.singletonMap("X", 3.3)))
        Dataset input =  new Dataset<>(MoleculeObject.class, mols)
        input.generateMetadata()


        ScriptEngine engine = GroovyScriptExecutor.createScriptEngine(this.getClass().getClassLoader())

        when:
        Dataset output = GroovyScriptExecutor.executeAndReturnValue(Dataset.class, engine, script, [input:input]);
        output.generateMetadata()

        then:
        output != null
        println "result: $output"
        println "result metadata: ${output.metadata}"

    }
}
