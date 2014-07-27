package webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class DatasetCanvasItemPanel extends Panel {

    private final DatasetCanvasItemModel model;

    public DatasetCanvasItemPanel(String id, DatasetCanvasItemModel model) {
        super(id);
        this.model = model;
        setOutputMarkupId(true);
        add(new Label("id", model.getId()));
    }
}
