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

package org.squonk.execution.steps.impl;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.util.IOUtils;

import java.util.logging.Logger;

/**
 * Created by timbo on 16/07/16.
 */
public abstract class AbstractDockerScriptRunnerStep extends AbstractContainerStep {

    private static final Logger LOG = Logger.getLogger(AbstractDockerScriptRunnerStep.class.getName());

    public static final String OPTION_DOCKER_IMAGE = StepDefinitionConstants.OPTION_DOCKER_IMAGE;
    public static final String OPTION_SCRIPT = StepDefinitionConstants.TrustedGroovyDataset.OPTION_SCRIPT;

    protected static String ISOLATED_NETWORK_NAME = IOUtils.getConfiguration("ISOLATED_NETWORK_NAME", "deploy_squonk_isolated");


}
