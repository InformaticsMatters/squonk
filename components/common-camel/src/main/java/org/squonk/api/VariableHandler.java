package org.squonk.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** Interfaces for a class that wants to define how it will be persisted using the Notebook/Variable API.
 * To conform the class must:
 * <ol>
 *     <li>Implement this interface</li>
 *     <li>Have a constructor with a single parameter of {@link ReadContext} or, for handling generic types, two parameters
 *     of types {java.lang.Class} and {@link ReadContext}</li>
 * </ol>
 * This allows the implementation to define exactly how it is persisted, using text and stream values.
 * For an example see {@link org.squonk.dataset.Dataset} which needs to persist 2 values separately, one for the metadata
 * and one for the actual (potentially very large) stream of data.
 *
 * Created by timbo on 13/03/16.
 */
public interface VariableHandler<T> {

    Class<T> getType();

    /** Write the variable using the Notebook/Variable API
     *
     * @param value The value to be writen
     * @param context The  WriteContext to write the variable to
     */
    void writeVariable(T value, WriteContext context) throws Exception;


    /** Read the variable using the Notebook/Variable API
     *
     * @param context
     * @return
     * @throws IOException
     */
    T readVariable(ReadContext context) throws Exception;


    /** Context that allows a value to be read.
     *
     */
    interface ReadContext {
        String readTextValue(String key) throws Exception;
        InputStream readStreamValue(String key) throws Exception;
        default String readTextValue() throws Exception { return readTextValue((String)null);}
        default InputStream readStreamValue() throws Exception { return readStreamValue((String)null);}
    }

    /** Context that allows a value to be written.
     *
     */
    interface WriteContext {
        void writeTextValue(String value, String key) throws Exception;
        void writeStreamValue(InputStream value, String key) throws Exception;
        default void writeTextValue(String value) throws Exception { writeTextValue(value, null);}
        default void writeStreamValue(InputStream value) throws Exception { writeStreamValue(value, null);}
    }
}
