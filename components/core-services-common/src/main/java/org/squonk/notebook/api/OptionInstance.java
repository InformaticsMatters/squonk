package org.squonk.notebook.api;

import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class OptionInstance implements Serializable {
    private final static long serialVersionUID = 1l;
    private OptionDescriptor optionDescriptor;
    private Object value;
    private boolean dirty = true;

    public Object getValue() {
        return value;
    }

    public <T> T getValue(Class<T> type) {
        return (T)getValue();
    }

    public void setValue(Object value) {
        dirty = true;
        this.value = value;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }

    public OptionDescriptor getOptionDescriptor() {
        return optionDescriptor;
    }

    public void setOptionDescriptor(OptionDescriptor optionDescriptor) {
        this.optionDescriptor = optionDescriptor;
    }
}
