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

package org.squonk.core.exec;

import java.util.Map;

/**
 * Base class for service execution
 *
 * @author timbo
 */
public class ServiceExecutor {

    /**
     * The HTTP endpoint to which the content should be POSTed as the body.
     */
    private String endpoint;

    /**
     * Parameters for the invocation. The keys must submit with .header or .query
     * to specify of they are query or header invocationParameters. Example: a
     * key of header.min_clusters specifies that a header name min_clusters
     * should be set with the corresponding value.
     */
    private Map<String, String> invocationParameters;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Map<String, String> getInvocationParameters() {
        return invocationParameters;
    }

    public void setInvocationParameters(Map<String, String> parameters) {
        this.invocationParameters = parameters;
    }

}
