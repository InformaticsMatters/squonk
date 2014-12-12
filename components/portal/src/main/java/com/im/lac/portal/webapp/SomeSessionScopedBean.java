package com.im.lac.portal.webapp;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

@SessionScoped
public class SomeSessionScopedBean implements Serializable {

    private String something = "something";

    public String getSomething() {
        return something;
    }

    public void setSomething(String something) {
        this.something = something;
    }
}
