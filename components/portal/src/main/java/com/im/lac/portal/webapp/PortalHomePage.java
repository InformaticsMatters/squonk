package com.im.lac.portal.webapp;

import com.im.lac.wicket.semantic.NotifierProvider;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;

import javax.inject.Inject;

public class PortalHomePage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;

    public PortalHomePage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addShowNotifierAction();
    }

    private void addShowNotifierAction() {
        add(new AjaxLink("notify") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                notifierProvider.getNotifier(PortalHomePage.this).notify("Title", "Some message here");
            }
        });
    }
}
