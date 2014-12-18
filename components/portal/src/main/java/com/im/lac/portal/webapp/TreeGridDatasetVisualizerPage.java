package com.im.lac.portal.webapp;

import com.im.lac.wicket.semantic.NotifierProvider;
import org.apache.wicket.markup.html.WebPage;

import javax.inject.Inject;

public class TreeGridDatasetVisualizerPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;

    public TreeGridDatasetVisualizerPage() {
        notifierProvider.createNotifier(this, "notifier");
        addVisualizerPanel();
    }

    private void addVisualizerPanel() {
        add(new VisualizerPanel("visualizerPanel"));
    }
}
