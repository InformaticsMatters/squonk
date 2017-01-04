package org.squonk.io;

import org.squonk.core.Descriptor;
import org.squonk.types.io.JsonHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by timbo on 27/12/16.
 */
public class DescriptorLoader<T extends Descriptor> {

    private final String id;
    private final URL url;
    private final Class<T> type;
    private volatile T descriptor;

    public DescriptorLoader(URL url, String id, Class<T> type) {
        this.url = url;
        this.id = id;
        this.type = type;
    }

    public DescriptorLoader(URL url, T descriptor) {
        this.url = url;
        this.type = (Class<T>)(descriptor.getClass());
        this.descriptor = descriptor;
        this.id = descriptor.getId();
    }

    public String getId() {
        return id;
    }

    public URL getURL() {
        return url;
    }

    public Class<T> getType() {
        return type;
    }

    /** Load the resource
     *
     * @return
     * @throws IOException
     */
    public T load() throws IOException {
        if (descriptor == null) {
            try (InputStream is = url.openStream()) {
                descriptor = JsonHandler.getInstance().objectFromJson(is, type);
            }
        }
        return descriptor;
    }

    /** Load a resource that is defined relative to this resource. Uses new URL(url, path) to generate the relative URL
     * to load.
     *
     * @param path
     * @return
     * @throws IOException
     */
    public InputStream loadRelative(String path) throws IOException {
        URL res = new URL(url, path);
        return res.openStream();
    }
}
