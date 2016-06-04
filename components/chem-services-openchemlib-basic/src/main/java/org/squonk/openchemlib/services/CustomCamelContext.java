package org.squonk.openchemlib.services;

import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.ThreadPoolProfile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * Created by timbo on 04/06/16.
 */
@ApplicationScoped
public class CustomCamelContext extends DefaultCamelContext {

    private static final Logger LOG = Logger.getLogger(CustomCamelContext.class.getName());

    public static final String THREAD_POOL_PROFILE = "customThreadPool";

    @PostConstruct
    void customize() {
        ThreadPoolProfile profile = new ThreadPoolProfileBuilder(THREAD_POOL_PROFILE).poolSize(4).maxPoolSize(25).build();
        getExecutorServiceManager().registerThreadPoolProfile(profile);
        LOG.info("Custom thread pool created for OCL with pool size=4 and max=25");
    }

    @PreDestroy
    void cleanUp() {
        // ...
    }
}
