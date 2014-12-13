package com.im.lac.wicket.semantic;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class NotifierPanel extends Panel {

    private String title;
    private String message;
    private NotifierBehavior notifierBehavior;

    public NotifierPanel(String id) {
        super(id);
        setOutputMarkupId(true);

        notifierBehavior = new NotifierBehavior();
        add(notifierBehavior);

        add(new Label("title", new PropertyModel<String>(this, "title")).setEscapeModelStrings(false));
        add(new Label("message", new PropertyModel<String>(this, "message")).setEscapeModelStrings(false));
    }

    public void notify(String title, String message) {
        this.title = title;
        this.message = message;
        AjaxRequestTarget ajaxRequestTarget = getRequestCycle().find(AjaxRequestTarget.class);
        ajaxRequestTarget.add(this);
        ajaxRequestTarget.appendJavaScript("" +
                "clearTimeout(window.notifierTimeout);\n;" +
                "$('#" + notifierBehavior.getMarkupId() + "').find('.notifier').show();\n" +
                "window.notifierTimeout = window.setTimeout(function(){ $('#" + notifierBehavior.getMarkupId() + "').find('.notifier').hide() }, 5000);\n");
    }
}
