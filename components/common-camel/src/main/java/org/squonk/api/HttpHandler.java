package org.squonk.api;

import org.squonk.http.HttpExecutor;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public interface HttpHandler<T> {

    void prepareRequest(T obj, HttpExecutor executor) throws IOException;
    T readResponse(HttpExecutor executor) throws IOException;

//    interface HttpRequestContext {
//
//        void addRequestHeader(String name, String value);
//        void setRequestBody(InputStream is);
//
//    }
//
//    interface HttpResponseContext {
//
//        void execute();
//        InputStream getResponseBody();
//        String getResponseHeader(String name);
//
//    }

}
