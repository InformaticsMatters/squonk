package org.squonk.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by timbo on 13/03/16.
 */
public interface Variable<T> {

    Writer<T> getVariableWriter();

    interface Writer<T> {
        void write(WriteContext context) throws IOException;
    }

    interface ReadContext {
        String readTextValue(String key) throws IOException;
        InputStream readStreamValue(String key) throws IOException;
        URL getTextValueUrl(String key);
        URL getStreamValueUrl(String key);
        default String readTextValue() throws IOException { return readTextValue((String)null);}
        default InputStream readStreamValue() throws IOException { return readStreamValue((String)null);}
        default URL getTextValueUrl() { return getTextValueUrl(null);}
        default URL getStreamValueUrl() { return getStreamValueUrl(null);}
        String readTextValue(URL url) throws IOException;
        InputStream readStreamValue(URL url) throws IOException;
    }

    interface WriteContext {
        void writeTextValue(String value, String key) throws IOException;
        void writeStreamValue(InputStream value, String key) throws IOException;
        default void writeTextValue(String value) throws IOException { writeTextValue(value, null);}
        default void writeStreamValue(InputStream value) throws IOException { writeStreamValue(value, null);}
    }
}
