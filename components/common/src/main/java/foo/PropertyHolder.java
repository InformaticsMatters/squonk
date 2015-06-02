package foo;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class PropertyHolder {

    private Map<String, Object> values = new HashMap<>();

    Object putProperty(String name, Object value) {
        return values.put(name, value);
    }

    public Map<String, Object> getValues() {
        return values;
    }

}
