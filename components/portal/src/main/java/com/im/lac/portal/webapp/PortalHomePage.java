package com.im.lac.portal.webapp;

import org.apache.wicket.markup.html.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class PortalHomePage extends WebPage {

    private static final Logger logger = LoggerFactory.getLogger(PortalHomePage.class.getName());

    @Inject
    private SomeSessionScopedBean someSessionScopedBean;

    public PortalHomePage() {
        logger.info(someSessionScopedBean.getSomething());
    }
}
