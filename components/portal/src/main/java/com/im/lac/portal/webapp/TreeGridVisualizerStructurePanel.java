package com.im.lac.portal.webapp;

import org.apache.wicket.markup.html.panel.Panel;

public class TreeGridVisualizerStructurePanel extends Panel {

    public TreeGridVisualizerStructurePanel(String id, Long datasetId, Long rowId) {
        super(id);
        add(new ExternalStructureImage("image", rowId.toString(), datasetId.toString(), "structureImageResource"));
    }

}
