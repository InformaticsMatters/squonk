package org.squonk.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by timbo on 05/12/16.
 */
public class URLResource implements Resource {

    private final String resource;

    public URLResource(String resource) {
        this.resource = resource;
    }

    @Override
    public InputStream get() throws IOException {
        URL url = new URL(resource);
        return url.openStream();
    }
}
