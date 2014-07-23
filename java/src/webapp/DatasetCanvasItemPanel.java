package webapp;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class DatasetCanvasItemPanel extends Panel {

    private DatasetCanvasItemModel model;

    public DatasetCanvasItemPanel(String id, DatasetCanvasItemModel model) {
        super(id);
        this.model = model;
        setOutputMarkupId(true);
    }
}
