package webapp;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class DatasetCanvasItemModel implements Serializable {

    private String initialX;
    private String initialY;
    private int id;

    public String getInitialX() {
        return initialX;
    }

    public void setPositionX(String initialX) {
        this.initialX = initialX;
    }

    public String getInitialY() {
        return initialY;
    }

    public void setPositionY(String initialY) {
        this.initialY = initialY;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
