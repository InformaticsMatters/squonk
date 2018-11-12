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

import org.apache.camel.CamelContext;
import org.squonk.execution.runners.ContainerRunner;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/** Step that executes a <a href="http://nextflow.io">Nextflow</a> workflow.
 * IMPORTANT: this is currently disabled as the security implications need more assessment.
 * Use the {@link DatasetNextflowInDockerExecutorStep} instead.
 *
 * IMPORTANT: Details of this step are subject to change.
 *
 * Created by timbo on 28/07/17.
 */
public class DatasetNextflowExecutorStep extends AbstractContainerStep {

    private static final Logger LOG = Logger.getLogger(DatasetNextflowExecutorStep.class.getName());
    protected static final String MSG_RUNNING_NEXTFLOW = "Running Nextflow";


//    @Override
//    public void execute(VariableManager varman, CamelContext context) throws Exception {
//
//        if (serviceDescriptor == null) {
//            throw new IllegalStateException("No service descriptor present ");
//        } else if (!(serviceDescriptor instanceof NextflowServiceDescriptor)) {
//            throw new IllegalStateException("Expected service descriptor to be a " +
//                    NextflowServiceDescriptor.class.getName() +
//                    "  but it was a " + serviceDescriptor.getClass().getName());
//        }
//        NextflowServiceDescriptor descriptor = (NextflowServiceDescriptor) serviceDescriptor;
//
//        LOG.info("Input types are " + IOUtils.joinArray(descriptor.getServiceConfig().getInputDescriptors(), ","));
//        LOG.info("Output types are " + IOUtils.joinArray(descriptor.getServiceConfig().getOutputDescriptors(), ","));
//
//        // this executes this cell
//        doExecute(varman, context, descriptor);
//        //LOG.info(varman.getTmpVariableInfo());
//    }
//
//    protected void doExecute(VariableManager varman, CamelContext camelContext, NextflowServiceDescriptor descriptor) throws Exception {
//
//        if (true) throw new RuntimeException("DatasetNextflowExecutorStep is currently disabled");
//
////        // generate the nextflow parameters from the user specified options.
////        // These are the equivalent of the -- commandline arguments that get passed to the nexflow
////        // processor as params e.g. --message 'Hello' and are the params defined at the top of the nextflow file.
////        // These need to be a Map of keys and values without the -- prefix.
////        // First we need to extract the relevant options
////        Map<String, Object> paramOpts = new LinkedHashMap<>();
////        options.forEach((k, v) -> {
////            if (k.startsWith("arg.") && v != null) {
////                LOG.info("Found argument " + k + " = " + v);
////                paramOpts.put(k.substring(4), v);
////            }
////        });
////        // now apply the templates that are defined in the NextflowServiceDescriptor.nextflowParams property
////        Map<String, String> params = null;
////        if (descriptor.getNextflowParams() != null && !descriptor.getNextflowParams().isEmpty()) {
////            params = GroovyUtils.expandValues(descriptor.getNextflowParams(), paramOpts);
////        } else {
////            params = Collections.emptyMap();
////        }
////
////        NextflowRunner runner = new NextflowRunner();
////        runner.init();
////
////        List<String> args = new ArrayList<>();
////        String nextflowFilePath = runner.getHostWorkDir().getPath() + "/nextflow.nf";
////        args.add(nextflowFilePath);
////
////        LOG.info("Nextflow execution: " + nextflowFilePath + " " +
////                params.entrySet().stream().map((e) -> "--" + e.getKey() + " " + e.getValue()).collect(Collectors.joining(" ")));
////        try {
////            // create input files
////            statusMessage = MSG_PREPARING_INPUT;
////
////            // write the nextflow file that executes everything
////            LOG.info("Writing nextflow.nf");
////            String nextflowFileContents = descriptor.getNextflowFile();
////            runner.writeInput("nextflow.nf", nextflowFileContents, false);
////
////            // write the nextflow config file if one is defined
////            String nextflowConfigContents = descriptor.getNextflowConfig();
////            if (nextflowConfigContents != null && !nextflowConfigContents.isEmpty()) {
////                LOG.info("Writing nextflow.config as:\n" + nextflowConfigContents);
////                runner.writeInput("nextflow.config", nextflowConfigContents, false);
////            } else {
////                LOG.info("No nextflow.config");
////            }
////
////            // write the input data
////            writeInputs(camelContext, descriptor, varman, runner);
////
////            // run the command
////            statusMessage = MSG_RUNNING_NEXTFLOW;
////            LOG.info("Executing ...");
////            long t0 = System.currentTimeMillis();
////            int status = runner.execute(args, params);
////            long t1 = System.currentTimeMillis();
////            float duration = (t1 - t0) / 1000.0f;
////            LOG.info(String.format("Executed in %s seconds with return status of %s", duration, status));
////
////            if (status != 0) {
////                String log = "Reason unknown"; // TODO get details of error
////                statusMessage = "Error: " + log;
////                LOG.warning("Execution errors: " + log);
////                throw new RuntimeException("Nextflow execution failed:\n" + log);
////            }
////
////            // handle the output
////            statusMessage = MSG_PREPARING_OUTPUT;
////            readOutputs(camelContext, descriptor, varman, runner);
////
////            Properties props = runner.getFileAsProperties("output_metrics.txt");
////            generateMetricsAndStatus(props, duration);
////
////        } finally {
////            // cleanup
////            if (DEBUG_MODE < 2) {
////                runner.cleanup();
////                LOG.info("Results cleaned up");
////            }
////        }
//    }

    @Override
    public Map<String, Object> doExecute(Map<String, Object> inputs, CamelContext context) throws Exception {
        // TODO - remove the need for this
        throw new RuntimeException("Not implementable");
    }

    @Override
    protected ContainerRunner prepareContainerRunner() throws IOException {
        throw new RuntimeException("NYI");
    }
}
