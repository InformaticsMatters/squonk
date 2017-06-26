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

package org.squonk.core;

import org.squonk.io.IODescriptor;
import org.squonk.io.IORoute;

/** Interface that defined an executable service. From an instance of this class you can obtain a @{link ServiceConfig} instance
 * that defines the properties that are needed by a client of this service.
 * Implementations of this interface define the details of how the service is to be executed.
 *
 * The way the IODescriptors are defined and used needs special attention. The serviceConfig property defines IODescriptors
 * for input and output that MUST be defined. This defines the IO characteristics that any client must understand.
 * This ServiceDescriptor defines input and output IORoutes that define how the data is physically supplied to the service
 * (e.g. as File) and this can optionally defined an IODescriptor that specifies how the data is written.
 * The @{link #resolveInputIODescriptors} and @{link #resolveoutputIODescriptors} should be used to get the actual IODescriptors
 * to use.
 *
 *
 * Created by timbo on 04/01/17.
 */
public interface ServiceDescriptor {

    String getId();

    /** The descriptor that defines the client side properties of the service
     *
     * @return
     */
    ServiceConfig getServiceConfig();

    IORoute[] getInputRoutes();

    IORoute[] getOutputRoutes();

    /** Resolve the input IODescriptors that are to be used at runtime. If getInputRoutes() is not null these they are used.
     * If not, or one of the IORoutes is null, or its IODescriptor is null then fallback to the input IODescriptors from
     * the serviceConfig are used.
     * Implicit in the logic is that the IODescriptor for the serviceConfig MUST be defined, and MUST NOT be null.
     *
     * @return
     */
    IODescriptor[] resolveInputIODescriptors();

    /** Resolve the output IODescriptors that are to be used at runtime. Logic is similar to that used for resolveInputIODescriptors()
     *
     * @return
     */
    IODescriptor[] resolveOutputIODescriptors();

}
