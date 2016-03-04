package org.squonk.util;

import javax.script.*;
import java.util.Map;

/**
 * Created by timbo on 29/12/15.
 */
public class GroovyScriptExecutor {


    public static <T> T executeAndReturnValue(Class<T> type, ScriptEngine engine, String script, Map<String,Object> bindingVars) throws ScriptException {

        Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        if (bindingVars != null && !bindingVars.isEmpty()) {
            bindings.putAll(bindingVars);
        }
        return (T)engine.eval(script, bindings);

    }

    public static String addImportsToScript(String script, String... imports) {
        StringBuilder b = new StringBuilder();
        for (String imp : imports) {
            b.append("import ").append(imp).append("\n");
        }
        b.append("\n");
        b.append(script);
        return b.toString();
    }



    public static ScriptEngine createScriptEngine(ClassLoader classLoader) {

        ScriptEngineManager manager = new ScriptEngineManager(classLoader);
        ScriptEngine engine = manager.getEngineByName("Groovy");
        return engine;

    }

}
