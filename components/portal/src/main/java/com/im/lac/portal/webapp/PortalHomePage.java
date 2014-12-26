package com.im.lac.portal.webapp;

import com.im.lac.wicket.semantic.NotifierProvider;
import com.im.lac.wicket.semantic.SemanticResourceReference;
import com.inmethod.grid.common.AbstractGrid;
import com.vaynberg.wicket.select2.ApplicationSettings;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import javax.inject.Inject;

public class PortalHomePage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    private AjaxLink gridViewLink;
    private AjaxLink cardViewLink;
    private DatasetGridViewPanel datasetGridViewPanel;
    private DatasetCardViewPanel datasetCardViewPanel;

    public PortalHomePage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addPanels();
        addActions();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(AbstractGrid.class, "res/style.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(ApplicationSettings.class, "res/select2.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/semantic-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/easygrid-overrides.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/lac.js")));
    }

    private void addActions() {
        gridViewLink = new AjaxLink("datasetGridView") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                datasetGridViewPanel.setVisible(true);
                datasetCardViewPanel.setVisible(false);
                ajaxRequestTarget.add(datasetGridViewPanel);
                ajaxRequestTarget.add(datasetCardViewPanel);
                ajaxRequestTarget.add(cardViewLink);
                ajaxRequestTarget.add(gridViewLink);
                ajaxRequestTarget.appendJavaScript("makeMenuItemActive('" + gridViewLink.getMarkupId() + "')");
            }
        };
        gridViewLink.setOutputMarkupId(true);
        add(gridViewLink);

        cardViewLink = new AjaxLink("datasetCardView") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                datasetCardViewPanel.setVisible(true);
                datasetGridViewPanel.setVisible(false);
                ajaxRequestTarget.add(datasetCardViewPanel);
                ajaxRequestTarget.add(datasetGridViewPanel);
                ajaxRequestTarget.add(cardViewLink);
                ajaxRequestTarget.add(gridViewLink);
                ajaxRequestTarget.appendJavaScript("makeMenuItemActive('" + cardViewLink.getMarkupId() + "')");
            }
        };
        cardViewLink.setOutputMarkupId(true);
        add(cardViewLink);
    }

    private void addPanels() {
        datasetGridViewPanel = new DatasetGridViewPanel("datasetGridViewPanel");
        datasetGridViewPanel.setOutputMarkupId(true);
        datasetGridViewPanel.setOutputMarkupPlaceholderTag(true);
        add(datasetGridViewPanel);

        datasetCardViewPanel = new DatasetCardViewPanel("datasetCardViewPanel");
        datasetCardViewPanel.setOutputMarkupId(true);
        datasetCardViewPanel.setOutputMarkupPlaceholderTag(true);
        add(datasetCardViewPanel);
        datasetCardViewPanel.setVisible(false);
    }

}
