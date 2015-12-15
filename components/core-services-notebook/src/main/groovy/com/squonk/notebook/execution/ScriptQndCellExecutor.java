package com.squonk.notebook.execution;


import org.squonk.notebook.client.CallbackClient;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.squonk.notebook.api.CellType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class ScriptQndCellExecutor implements QndCellExecutor {
    private static final Logger LOGGER = Logger.getLogger(ScriptQndCellExecutor.class.getName());

    @Inject
    protected CallbackClient callbackClient;

    @Override
    public boolean handles(CellType cellType) {
        return "Script".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {

//        CellDTO cell = callbackClient.retrieveCell(cellName);
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("Groovy");
//        Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
//        cell.getInputVariableList().clear();
//        for (Cell other : notebookContents.getCellList()) {
//            if (other != cell) {
//                for (Variable variable : other.getOutputVariableList()) {
//                    if (variable.getValue() != null) {
//                        String producerName = variable.getProducerCell().getName().replaceAll(" ", "_");
//                        bindings.put(producerName + "_" + variable.getName(), variable.getValue());
//                    }
//                }
//            }
//        }
//        try {
//            String code = (String)cell.getPropertyMap().get("code");
//            Object result = scriptToVm(engine.eval(code));
//            cell.getPropertyMap().put("outcome", result);
//            cell.getPropertyMap().put("errorMessage", null);
//            cell.getOutputVariableList().get(0).setValue(result);
//        } catch (ScriptException se) {
//            LOGGER.log(Level.WARNING, se.getMessage());
//            cell.getPropertyMap().put("errorMssage", se.getMessage());
//        }
//         notebookService.storeNotebookContents(callbackContext.getNotebookId(), notebookContents);

    }

    private Object scriptToVm(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror)o;
            Collection<Object> result = new ArrayList<Object>();
            Collection<Object> values = scriptObjectMirror.values();
            for (Object value : values) {
                result.add(scriptToVm(value));
            }
            return result;
        } else {
            return o;
        }
    }

}
