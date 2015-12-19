package org.squonk.notebook.api;

import com.squonk.util.Utils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VariableKey implements Serializable {
    private String producerName;
    private String name;

    public VariableKey() {}

    public VariableKey(String producerName, String name) {
        this.producerName = producerName;
        this.name = name;
    }


    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "VariableKey [producerName:" + producerName + " name:" + name + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof VariableKey)) {
            return false;
        }
        return Utils.safeEquals(this.producerName, ((VariableKey)obj).producerName) && Utils.safeEquals(this.name, ((VariableKey)obj).name);
    }

    @Override
    public int hashCode() {
        return (producerName + name).hashCode();
    }
}
