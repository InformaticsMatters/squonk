package com.im.lac.wicket.inmethod;

import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;

/**
 * @author simetrias
 */
public abstract class RowActionsCallbackHandler<T> implements Serializable {

    public abstract void onAction(AjaxRequestTarget target, String name, T rowModelObject);

    public boolean isActionVisible(String actionName, T rowModelObject) {
        return true;
    }

    public boolean isEnabled(String actionName, T rowModelObject) {
        return true;
    }

    public String getEnabledTitle(String actionName, T rowModelObject) {
        return null;
    }

    public String getDisabledTitle(String actionName, T rowModelObject) {
        return null;
    }

    public String getEnabledClass(String actionName, T rowModelObject) {
        return null;
    }

    public String getDisabledClass(String actionName, T rowModelObject) {
        return null;
    }
}
