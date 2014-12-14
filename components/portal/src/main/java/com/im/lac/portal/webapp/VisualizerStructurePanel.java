package com.im.lac.portal.webapp;

import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.PackageResourceReference;

public class VisualizerStructurePanel extends Panel {

    public VisualizerStructurePanel(String id) {
        super(id);
        add(new NonCachingImage("image", new PackageResourceReference(VisualizerStructurePanel.class, "resources/structure.png")));
    }

}
