package org.squonk.api;

import org.squonk.http.RequestResponseExecutor;

import java.io.IOException;

/**
 * Created by timbo on 23/03/2016.
 */
public interface HttpHandler<T> {

    Class<T> getType();
    void prepareRequest(T obj, RequestResponseExecutor executor, boolean gzip) throws IOException;
    void writeResponse(T obj, RequestResponseExecutor executor, boolean gzip) throws IOException;
    T readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException;

}
