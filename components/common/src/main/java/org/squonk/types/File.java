package org.squonk.types;

/**
 * Created by timbo on 07/06/17.
 */
public class File<T extends AbstractStreamType> {

    private final Class<T> type;

    public File(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }
}
