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

package org.squonk.core.camel;

import org.apache.camel.component.servletlistener.CamelContextLifecycle;
import org.apache.camel.component.servletlistener.ServletCamelContext;
import org.apache.camel.impl.SimpleRegistry;

/**
 *
 * @author timbo
 */
public class ServletCamelLifeCycle implements CamelContextLifecycle<SimpleRegistry> {

    private final CamelLifeCycle worker = new CamelLifeCycle();

    @Override
    public void beforeStart(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        worker.beforeStart(scc, r);
    }

    @Override
    public void afterStart(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        worker.afterStart(scc, r);
    }

    @Override
    public void beforeStop(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        worker.beforeStop(scc, r);
    }

    @Override
    public void afterStop(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        worker.afterStop(scc, r);
    }

    @Override
    public void beforeAddRoutes(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        worker.beforeAddRoutes(scc, r);
    }

    @Override
    public void afterAddRoutes(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        worker.afterAddRoutes(scc, r);
    }

}
