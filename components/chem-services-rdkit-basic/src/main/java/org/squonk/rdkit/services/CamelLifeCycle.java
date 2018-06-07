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

package org.squonk.rdkit.services;

import org.apache.camel.ProducerTemplate;
import org.squonk.camel.CamelCommonConstants;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.component.servletlistener.CamelContextLifecycle;
import org.apache.camel.component.servletlistener.ServletCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.ThreadPoolProfile;

/**
 *
 * @author timbo
 */
public class CamelLifeCycle implements CamelContextLifecycle<SimpleRegistry> {

    private static final Logger LOG = Logger.getLogger(CamelLifeCycle.class.getName());

    @Override
    public void beforeStart(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.beforeStart()");
    }

    @Override
    public void afterStart(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.afterStart()");

        LOG.info("Posting service descriptors");
        try {
            ProducerTemplate pt = scc.createProducerTemplate();
            String result = pt.requestBody(RdkitBasicRestRouteBuilder.ROUTE_POST_SDS, "", String.class);
            LOG.info("Response was: " + result);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to post service descriptors", e);
        }
    }

    @Override
    public void beforeStop(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.beforeStop()");
    }

    @Override
    public void afterStop(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.afterStop()");
    }

    @Override
    public void beforeAddRoutes(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.beforeAddRoutes()");
        LOG.fine("Creating custom thread pool profile named " + CamelCommonConstants.CUSTOM_THREAD_POOL_NAME);
        ThreadPoolProfile profile = new ThreadPoolProfileBuilder(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME).poolSize(4).maxPoolSize(50).build();
        scc.getExecutorServiceManager().registerThreadPoolProfile(profile);
    }

    @Override
    public void afterAddRoutes(ServletCamelContext scc, SimpleRegistry r) throws Exception {
        LOG.fine("CamelLifeCycle.afterAddRoutes()");
    }

}
