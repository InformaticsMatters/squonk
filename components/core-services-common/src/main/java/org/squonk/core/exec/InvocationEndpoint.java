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

/**
 *
 * @author timbo
 */
public class InvocationEndpoint {

    /**
     * e.g. http://some.url/foo/service1/XYZ123 or XYZ123 if its relative to the 
     * original service request
     */
    private String endpoint;
    
    /**
    * e.g. ["metrics", "metadata"]
    */
    private String[] additionalRequests;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String[] getAdditionalRequests() {
        return additionalRequests;
    }

    public void setAdditionalRequests(String[] additionalRequests) {
        this.additionalRequests = additionalRequests;
    }

}
