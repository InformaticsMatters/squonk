package webapp;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
@SessionScoped
public class SessionData implements Serializable {

    private List<DatasetCanvasItemModel> canvasItemList = new ArrayList<DatasetCanvasItemModel>();

    public List<DatasetCanvasItemModel> getCanvasItemList() {
        return canvasItemList;
    }

}
