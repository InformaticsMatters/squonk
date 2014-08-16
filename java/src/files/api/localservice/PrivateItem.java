package files.api.localservice;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author simetrias
 */
@XmlRootElement
public class PrivateItem implements Serializable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
