package com.im.lac.portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class MenuPanel extends Panel {

    private AjaxLink homeLink;
    private AjaxLink uploadPanelLink;

    public MenuPanel(String id) {
        super(id);
        addActions();
    }

    private void addActions() {
        homeLink = new AjaxLink("home") {

            @Override
            public void onClick(AjaxRequestTarget target) {
            }
        };
        add(homeLink);

        uploadPanelLink = new AjaxLink("upload") {

            @Override
            public void onClick(AjaxRequestTarget target) {
            }
        };
        add(uploadPanelLink);

        add(new Label("username", new PropertyModel<String>(this, "getUserName")));

        add(new Link<String>("logout") {

            @Override
            public void onClick() {
            }
        });
    }

    public String getUserName() {
        return "User Name";
    }
}
