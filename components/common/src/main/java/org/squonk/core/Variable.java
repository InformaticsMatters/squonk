package org.squonk.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** Interfaces for a class that wants to define how it will be persisted using the Notebook/Variable API.
 * To conform the class must:
 * <ol>
 *     <li>Implement this interface</li>
 *     <li>Have a constructor with a single parameter of {@link ReadContext} or, for handleing generic types, two parameters
 *     of types {java.lang.Class} and {@link ReadContext}</li>
 * </ol>
 * This allows the implementation to define exactly how it is persisted, using text and stream values.
 * For an example see {@link org.squonk.dataset.Dataset} which needs to persist 2 values separately, one for the metadata
 * and one for the actual (potentially very large) stream of data.
 *
 * Created by timbo on 13/03/16.
 */
public interface Variable<T> {

    /** Get a writer that knows how to save the value.
     *
     * @return
     */
    Writer<T> getVariableWriter();

    /** Interface that allows data to be written using the Notebook/Variable API
     *
     * @param <T>
     */
    interface Writer<T> {
        void write(WriteContext context) throws IOException;
    }

    /** Context that allows a value to be read.
     *
     */
    interface ReadContext {
        String readTextValue(String key) throws IOException;
        InputStream readStreamValue(String key) throws IOException;

        /** As for the {@link #getStreamValueUrl} method, but for getting a text value. Use the  {@link #readTextValue(URL))}
         * method to get the data when ready.
         *
         * @return
         */
        URL getTextValueUrl(String key);

        /** Allows to defer reading the value till later by providing a URL to the value.
         * This assumes a RESTful approach to fetching values.
         * If using this approach then you should use the {@link #readStreamValue(URL))} method when you need to get the value
         * as this will know how to handle authentication.
         *
         * @return
         */
        URL getStreamValueUrl(String key);
        default String readTextValue() throws IOException { return readTextValue((String)null);}
        default InputStream readStreamValue() throws IOException { return readStreamValue((String)null);}

        default URL getTextValueUrl() { return getTextValueUrl(null);}

        default URL getStreamValueUrl() { return getStreamValueUrl(null);}

        /** Get the data for this URL representing a variable value that has previously been generated with the
         * {@link #getTextValueUrl)} methods. This method will know how to do any authentication that is necessary
         *
         * @param url
         * @return
         * @throws IOException
         */
        String readTextValue(URL url) throws IOException;

        /** Get the data for this URL representing a variable value that has previously been generated with the
         * {@link #getStreamValueUrl)} methods. This method will know how to do any authentication that is necessary
         *
         * @param url
         * @return
         * @throws IOException
         */
        InputStream readStreamValue(URL url) throws IOException;
    }

    /** Context that allows a value to be written.
     *
     */
    interface WriteContext {
        void writeTextValue(String value, String key) throws IOException;
        void writeStreamValue(InputStream value, String key) throws IOException;
        default void writeTextValue(String value) throws IOException { writeTextValue(value, null);}
        default void writeStreamValue(InputStream value) throws IOException { writeStreamValue(value, null);}
    }
}
