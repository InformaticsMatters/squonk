package com.im.lac.wicket.inmethod;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class CheckBoxColumnCallbackHandler<T> implements Serializable {

    public boolean isEnabled(T rowModelObject) {
        return true;
    }

    public String getDisabledTitle(T rowModelObject) {
        return null;
    }
}
