package org.squonk.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public interface RequestResponseExecutor {

    void prepareRequestHeader(String name, String value);
    void prepareRequestBody(InputStream is);
    void setResponseHeader(String name, String value);
    void setResponseBody(InputStream is);
    void execute() throws IOException;
    InputStream getResponseBody();
    String getResponseHeader(String name);
}
