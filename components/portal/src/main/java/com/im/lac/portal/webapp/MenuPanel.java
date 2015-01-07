package com.im.lac.portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class MenuPanel extends Panel {

    private AjaxLink homeLink;
    private AjaxLink descriptorsLink;
    private AjaxLink uploadPanelLink;

    public MenuPanel(String id) {
        super(id);
        addActions();
    }

    private void addActions() {

        homeLink = new AjaxLink("home") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(PortalHomePage.class);
            }
        };
        add(homeLink);

        descriptorsLink = new AjaxLink("descriptors") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(DescriptorsPage.class);
            }
        };
        add(descriptorsLink);

        add(new Link<String>("logout") {

            @Override
            public void onClick() {
            }
        });

        add(new Label("username", new PropertyModel<String>(this, "getUserName")));
    }

    public String getUserName() {
        return "User Name";
    }
}
