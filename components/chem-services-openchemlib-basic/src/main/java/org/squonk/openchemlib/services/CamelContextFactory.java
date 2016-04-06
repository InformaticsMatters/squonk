package org.squonk.openchemlib.services;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.ThreadPoolProfile;
import org.squonk.camel.CamelCommonConstants;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;


public class CamelContextFactory {

    @Produces
    @ApplicationScoped
    CamelContext customize() {
        DefaultCamelContext context = new DefaultCamelContext();
        // Set the Camel context name
        context.setName("openchemlib");
        // set the thread pool
        ThreadPoolProfile profile = new ThreadPoolProfileBuilder(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME).poolSize(4).maxPoolSize(50).build();
        context.getExecutorServiceManager().registerThreadPoolProfile(profile);
        return context;
    }

   void cleanUp(@Disposes CamelContext context) {
        // ...
    }

}