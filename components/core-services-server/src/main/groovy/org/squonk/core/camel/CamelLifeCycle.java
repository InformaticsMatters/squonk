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

import org.apache.camel.*;
import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.spi.ThreadPoolProfile;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.config.SquonkServerConfig;
import org.squonk.core.ServerConstants;
import org.squonk.core.service.discovery.PostgresServiceDescriptorClient;
import org.squonk.core.service.user.UserHandler;
import org.squonk.core.service.user.UserPostgresClient;
import org.squonk.core.util.Utils;
import org.squonk.core.service.discovery.ServiceDescriptorRegistry;
import org.squonk.util.ServiceConstants;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class CamelLifeCycle {

    private static final Logger LOG = Logger.getLogger(CamelLifeCycle.class.getName());
    

    private final DataSource dataSource;

    public CamelLifeCycle() {
        this(SquonkServerConfig.getSquonkDataSource());
    }

    public CamelLifeCycle(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void beforeStart(CamelContext context, SimpleRegistry r) throws Exception {
        //context.getShutdownStrategy().setTimeout(10);
        LOG.fine("beforeStart()");
        ThreadPoolProfile profile = new ThreadPoolProfileBuilder(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME).poolSize(4).maxPoolSize(50).build();
        context.getExecutorServiceManager().registerThreadPoolProfile(profile);
    }

    public void afterStart(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("afterStart()");

        LOG.info("Trying Java resolver");
        try {
            InetAddress address = InetAddress.getByName("chemservices");
            LOG.info("Addr: " + address.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public void beforeStop(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("beforeStop()");
        // noop
    }

    public void afterStop(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("afterStop()");

    }

    public void beforeAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("beforeAddRoutes()");
        r.put(ServerConstants.USER_HANDLER, new UserHandler(new UserPostgresClient(dataSource)));
        r.put(ServiceConstants.KEY_SERVICE_REGISTRY, new ServiceDescriptorRegistry(new PostgresServiceDescriptorClient()));
    }

    public void afterAddRoutes(CamelContext context, SimpleRegistry r) throws Exception {
        LOG.fine("afterAddRoutes()");
        // noop
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
