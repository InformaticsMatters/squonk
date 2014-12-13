package com.im.lac.wicket.semantic;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

public class SemanticModalPanel extends Panel {

    private SemanticModalPanel parentModalPanel;
    private WebMarkupContainer modalElementComponent;

    public SemanticModalPanel(String id, String modalElementWicketId) {
        super(id);
        setOutputMarkupId(true);
        modalElementComponent = new WebMarkupContainer(modalElementWicketId);
        modalElementComponent.setOutputMarkupId(true);
        add(modalElementComponent);
    }

    protected WebMarkupContainer getModalRootComponent() {
        return modalElementComponent;
    }

    public SemanticModalPanel getParentModalPanel() {
        return parentModalPanel;
    }

    public void setParentModalPanel(SemanticModalPanel parentModalPanel) {
        this.parentModalPanel = parentModalPanel;
    }

    public void showModal() {
        AjaxRequestTarget ajaxRequestTarget = getRequestCycle().find(AjaxRequestTarget.class);
        if (ajaxRequestTarget != null) {
            ajaxRequestTarget.add(getModalRootComponent());
            String template = "$('#:modalElement')" +
                    ".modal('setting', 'closable', false)" +
                    ".modal('setting', 'onApprove', function() { return false; })" +
                    ".modal('show')";
            String script = template.replaceAll(":modalElement", modalElementComponent.getMarkupId());
            ajaxRequestTarget.appendJavaScript(script);
            if (parentModalPanel != null) {
                String hideParent = "var $modalElement=$('#:parent');" +
                        "$modalElement.modal('hide')";
                ajaxRequestTarget.appendJavaScript(hideParent.replaceAll(":parent", parentModalPanel.getModalRootComponent().getMarkupId()));
            }
        }
    }

    public void hideModal() {
        AjaxRequestTarget ajaxRequestTarget = getRequestCycle().find(AjaxRequestTarget.class);
        if (ajaxRequestTarget != null) {
            if (parentModalPanel != null) {
                String showParent = "var $modalElement=$('#:parent');" +
                        "$modalElement.modal('show')";
                ajaxRequestTarget.prependJavaScript(showParent.replaceAll(":parent", parentModalPanel.getModalRootComponent().getMarkupId()));
            }
            String template = "var $modalElement=$('#:modalElement');" +
                    "$modalElement.modal('hide');" +
                    "$modalElement.remove()";
            String script = template.replaceAll(":modalElement", modalElementComponent.getMarkupId());
            ajaxRequestTarget.prependJavaScript(script);
            ajaxRequestTarget.add(this);
        }
    }
}
