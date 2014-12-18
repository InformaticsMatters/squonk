package com.im.lac.wicket.inmethod;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class RowActionsPanel<T extends Serializable> extends Panel {

    private final RowActionsCallbackHandler<T> callbackHandler;
    private final T rowModelObject;

    public RowActionsPanel(String id, List<String> actionNameList, T rowModelObject, RowActionsCallbackHandler<T> callbackHandler) {
        super(id);
        this.callbackHandler = callbackHandler;
        this.rowModelObject = rowModelObject;
        addActions(actionNameList);
    }

    private void addActions(List<String> actionNameList) {
        ListView<String> actionList = new ListView<String>("actions", actionNameList) {

            @Override
            protected void populateItem(ListItem<String> components) {
                final String actionName = components.getModelObject();
                final boolean isEnabled = callbackHandler.isEnabled(actionName, rowModelObject);

                AjaxLink action = new AjaxLink("action") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        if (isEnabled) {
                            callbackHandler.onAction(ajaxRequestTarget, actionName, rowModelObject);
                        }
                    }

                    @Override
                    public boolean isVisible() {
                        return callbackHandler.isActionVisible(actionName, rowModelObject);
                    }
                };
                setClass(action, actionName, isEnabled);
                setTitle(action, actionName, isEnabled);
                components.add(action);
            }
        };
        add(actionList);
    }

    private void setClass(AjaxLink ajaxLink, String actionName, boolean isEnabled) {
        String className = actionName;
        if (isEnabled) {
            String enabledClass = callbackHandler.getEnabledClass(actionName, rowModelObject);
            if (enabledClass != null) {
                className = enabledClass;
            }
        } else {
            String disabledClass = callbackHandler.getDisabledClass(actionName, rowModelObject);
            if (disabledClass != null) {
                className = disabledClass;
            } else {
                String enabledClass = callbackHandler.getEnabledClass(actionName, rowModelObject);
                if (enabledClass != null) {
                    className = enabledClass;
                }
            }
        }
        ajaxLink.add(new AttributeModifier("class", className));

        if (!isEnabled && callbackHandler.getDisabledClass(actionName, rowModelObject) == null) {
            ajaxLink.add(new AttributeModifier("style", "cursor: default;"));
        }
    }

    private void setTitle(AjaxLink ajaxLink, String actionName, boolean isEnabled) {
        String title;
        if (isEnabled) {
            title = callbackHandler.getEnabledTitle(actionName, rowModelObject);
        } else {
            title = callbackHandler.getDisabledTitle(actionName, rowModelObject);
        }
        if (title != null) {
            ajaxLink.add(new AttributeModifier("title", title));
        }
    }
}
