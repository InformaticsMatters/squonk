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

package org.squonk.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by timbo on 27/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IORoute {

    public enum Route {
        /**
         * A file in the file system, or an attachment for an HTTP request
         */
        FILE,
        /**
         * STDIN/STDOUT or the body of a HTTP POST or response
         */
        STREAM
    }

    private final Route route;
    private final IODescriptor descriptor;

    public IORoute(@JsonProperty("route") Route route, @JsonProperty("descriptor") IODescriptor descriptor) {
        this.route = route;
        this.descriptor = descriptor;
    }

    public IORoute(Route route) {
        this.route = route;
        this.descriptor = null;
    }

    public Route getRoute() {
        return route;
    }

    public IODescriptor getDescriptor() {
        return descriptor;
    }
}
