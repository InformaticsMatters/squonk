package com.im.lac.camel.rdkit;

import com.im.lac.camel.processor.MoleculeObjectSourcer;
import com.im.lac.types.MoleculeObject;
import edu.emory.mathcs.backport.java.util.Collections;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class RDKitMoleculeProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(RDKitMoleculeProcessor.class.getName());

    enum Mode {

        Calculate, Filter
    };

    List<Definition> definitions = new ArrayList<>();

    @Override
    public void process(Exchange exchange) throws Exception {
        MoleculeObjectSourcer sourcer = new MoleculeObjectSourcer() {
            @Override
            public void handleSingle(Exchange exchange, MoleculeObject mo) throws Exception {
                Iterator<MoleculeObject> results = evaluate(exchange, Collections.singletonList(mo).iterator(), definitions);
                if (results.hasNext()) {
                    exchange.getIn().setBody(results.next());
                } else {
                    // this can happen when filtering. May not to consitent with how
                    // ChemAxon equivalent handles things. Need to investigate.
                    exchange.getIn().setBody(null);
                }
            }

            @Override
            public void handleMultiple(Exchange exchange, Iterator<MoleculeObject> mols) throws Exception {
                Iterator<MoleculeObject> results = evaluate(exchange, mols, definitions);
                exchange.getIn().setBody(results);
            }
        };
        sourcer.handle(exchange);
    }

    Iterator<MoleculeObject> evaluate(
            Exchange exchange, Iterator<MoleculeObject> mols, List<Definition> definitions) throws ScriptException {
        /* TODO 
         Work out how to generate the result
         The definitions of what needs doing are in the definitions List.
         We need to pass that information to jython, along with the molecules.
         This example sets these to the binding as the mols and definitions properties.
         This way the jython script need to know nothing about Camel and the Exchange,
         just process the molecules.
         For a calculation the script would return the Iterable<MoleculeObject> with the calculated 
         properties added to the MoleculeObject using the putValue() method.
         For filtering the MoleculeObjects woould be added to the returned Iterable<MoleculeObject>
         only if the filter was passed.
         THERE MAY BE BETTER WAYS TO APPROACH THIS, but we need to make sure this only 
         executes the jython script once for each set of Iterable<MoleculeObject> inputs
         */

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");
        engine.put("mols", mols);
        engine.put("definitions", definitions);
        Reader script = getScriptReader("echo.py"); // TODO need the read script
        Iterator<MoleculeObject> result = null;
        try {
            LOG.info("Executing script");
            result = (Iterator<MoleculeObject>) engine.eval(script);
            LOG.info("Execution complete");
        } finally {
            try {
                script.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Failed to close stream", ex);
            }
        }
        return result;
    }

    Reader getScriptReader(String path) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
        return new InputStreamReader(is);
    }

    /**
     * Add a new calculation using an expression.
     *
     * TODO allow the expressions to be read from header to allow dynamic
     * configuration.
     * <br>
     * Note: the return type is the instance, to allow the fluent builder
     * pattern to be used.
     *
     * @param name The name for the calculated property
     * @param expression The chemical terms expression e.g. logP()
     * @return
     */
    public RDKitMoleculeProcessor calculate(String name, String expression) {
        definitions.add(Definition.calculate(name, expression));
        return this;
    }

    /**
     * Create a new filter based on a chemical terms expression. The expression
     * MUST evaluate to a boolean value. e.g. logP() &lt; 5
     *
     * @param expression
     * @return
     */
    public RDKitMoleculeProcessor filter(String expression) {
        definitions.add(Definition.filter(expression));
        return this;
    }

    /**
     * Currently 2 types are supported as defined by the Mode enum.
     *
     * 1. Calculate - calculates a property using the supplied expression and
     * stores it as the supplied propName
     *
     * 2. Filter - includes or excludes the MoleculeObject based of the supplied
     * expression
     *
     * Note: the ChemAxon equivalent also has these modes that may have RDkit
     * equivalents:
     *
     * 3. Transform - convert the molecule in some way e.g. leconformer()
     *
     * 4. Standardize - e.g. aromatize..removeExplicitH
     *
     */
    public static class Definition {

        public final Mode mode;
        public final String propName;
        public final String expression;

        Definition(Mode mode, String propName, String expression) {
            this.mode = mode;
            this.propName = propName;
            this.expression = expression;
        }

        static Definition calculate(String name, String expression) {
            return new Definition(Mode.Calculate, name, expression);
        }

        static Definition filter(String expression) {
            return new Definition(Mode.Filter, null, expression);
        }
    }

}
