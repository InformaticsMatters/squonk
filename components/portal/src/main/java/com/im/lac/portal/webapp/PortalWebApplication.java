package com.im.lac.portal.webapp;

import org.apache.wicket.Page;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.protocol.http.WebApplication;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

public class PortalWebApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return PortalHomePage.class;
    }

    @Override
    protected void init() {
        super.init();
        BeanManager beanManager = CDI.current().getBeanManager();
        new CdiConfiguration(beanManager).configure(this);
    }
}
