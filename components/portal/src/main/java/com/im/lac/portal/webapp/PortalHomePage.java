package com.im.lac.portal.webapp;

import org.apache.wicket.markup.html.WebPage;

import javax.inject.Inject;

public class PortalHomePage extends WebPage {

    @Inject
    private SomeSessionScopedBean someSessionScopedBean;

    public PortalHomePage() {
        System.out.println(someSessionScopedBean.getSomething());
    }
}
