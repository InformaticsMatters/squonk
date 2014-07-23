package webapp;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class DatasetCanvasItemModel implements Serializable {

    private String initialX;
    private String initialY;

    public String getInitialX() {
        return initialX;
    }

    public void setInitialX(String initialX) {
        this.initialX = initialX;
    }

    public String getInitialY() {
        return initialY;
    }

    public void setInitialY(String initialY) {
        this.initialY = initialY;
    }
}
