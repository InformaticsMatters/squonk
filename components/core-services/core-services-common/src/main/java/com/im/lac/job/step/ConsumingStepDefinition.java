/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.job.step;

import java.util.Map;

/**
 *
 * @author timbo
 */
public class ConsumingStepDefinition extends StepDefinition {

    private Map<String, String> variableNameMappings;

    public Map<String, String> getVariableNameMappings() {
        return variableNameMappings;
    }
    
    public final void configureConsumer(Map<String, String> variableNameMappings) {
        this.variableNameMappings = variableNameMappings;
    }

}
