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

/** Interface that defined an executable service. From an instance of this class you can obtain a @{link ServiceConfig} instance
 * that defines the properties that are needed by a client of this service.
 * Implementations of this interface define the details of how the service is to be executed.
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

}
