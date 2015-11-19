package com.squonk.notebook.execution;

import com.squonk.notebook.client.CallbackClient;
import com.squonk.notebook.api.CellDTO;
import com.squonk.notebook.api.CellType;
import com.squonk.notebook.api.VariableDTO;

import javax.inject.Inject;

public class Sample2QndCellExecutor implements QndCellExecutor {
    @Inject
    private CallbackClient callbackClient;

    @Override
    public boolean handles(CellType cellType) {
        return "Sample2".equals(cellType.getName());
    }



    @Override
    public void execute(String cellName) {
        CellDTO cell = callbackClient.retrieveCell(cellName);
        VariableDTO inputVariable = cell.getInputVariableList().get(0);
        Integer num1 = callbackClient.readIntegerValue(inputVariable.getProducerName(), inputVariable.getName());
        Integer num2 = (Integer) cell.getPropertyMap().get("number2");
        Integer result = (num1 == null || num2 == null) ? null : num1 + num2;
        String outputVariableName = cell.getOutputVariableNameList().get(0);
        callbackClient.writeIntegerValue(cellName, outputVariableName, result);
    }

}