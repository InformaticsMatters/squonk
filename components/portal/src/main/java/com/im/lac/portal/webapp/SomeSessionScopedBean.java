package com.im.lac.portal.webapp;

import javax.enterprise.context.SessionScoped;

@SessionScoped
public class SomeSessionScopedBean {

    private String something = "something";

    public String getSomething() {
        return something;
    }

    public void setSomething(String something) {
        this.something = something;
    }
}
