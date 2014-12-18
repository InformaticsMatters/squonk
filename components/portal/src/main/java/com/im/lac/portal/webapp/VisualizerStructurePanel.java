package com.im.lac.portal.webapp;

import org.apache.wicket.markup.html.panel.Panel;

public class VisualizerStructurePanel extends Panel {

    public VisualizerStructurePanel(String id, Long datasetId, Long rowId) {
        super(id);
        add(new ExternalStructureImage("image", datasetId.toString(), rowId.toString(), "structureImageResource"));
    }

}
