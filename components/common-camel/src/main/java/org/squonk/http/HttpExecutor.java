package org.squonk.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public interface HttpExecutor {

    void addRequestHeader(String name, String value);
    void setRequestBody(InputStream is);
    void execute() throws IOException;
    InputStream getResponseBody();
    String getResponseHeader(String name);
}
