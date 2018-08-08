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

package org.squonk.jobdef;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.core.ServiceDescriptor;

import java.util.Map;
import java.util.UUID;

/**
 * Created by timbo on 31/12/15.
 */
public class ExternalJobDefinition implements JobDefinition {

    //protected final IODescriptor[] inputs;
    //protected final IODescriptor[] outputs;
    protected final ServiceDescriptor serviceDescriptor;
    protected final Map<String, Object> options;
    protected final String jobId;

    public ExternalJobDefinition(
            @JsonProperty("serviceDescriptor") ServiceDescriptor serviceDescriptor,
            @JsonProperty("options") Map<String, Object> options) {
        this.serviceDescriptor = serviceDescriptor;
        this.options = options;
        this.jobId = UUID.randomUUID().toString();
    }

//    /** The inputs the execution accepts.
//     *
//     * @return
//     */
//    IODescriptor[] getInputs() {
//        return inputs;
//    }
//
//    /** The outputs the cell produces.
//     *
//     * @return
//     */
//    IODescriptor[] getOutputs() {
//        return outputs;
//    }


    public String getJobId() {
        return jobId;
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "ExternalJobDefinition: [jobId=" + jobId + "]";
    }
}
