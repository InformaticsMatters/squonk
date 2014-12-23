package com.im.lac.dwsearch

import javax.ws.rs.ApplicationPath
import org.glassfish.jersey.server.ResourceConfig

@ApplicationPath("/")
public class DWSearchApplication extends ResourceConfig {

    private static final String CONTROLLERS_PACKAGE_PREFIX = ".service";

    public DWSearchApplication() {
        // Add a package used to scan for components.
        packages(this.getClass().getPackage().getName() + CONTROLLERS_PACKAGE_PREFIX);
    }

}


