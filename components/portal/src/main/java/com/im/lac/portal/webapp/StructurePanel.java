package com.im.lac.portal.webapp;

import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.PackageResourceReference;

public class StructurePanel extends Panel {

    public StructurePanel(String id, RowData structureObject) {
        super(id);
        add(new NonCachingImage("image", new PackageResourceReference(StructurePanel.class, "resources/structure.png")));
    }

}
