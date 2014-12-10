package com.im.lac.portal;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

public class PortalWebApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return PortalHomePage.class;
    }
}
