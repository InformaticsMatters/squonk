package org.squonk.types;

import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public interface StreamType {

    /** Get all the data as it was generated, which might or might not be gzipped.
     *
     * @return
     */
    InputStream[] getInputStreams() throws IOException;

    String[] getStreamNames();

    /** Get the Gzipped compressed data items for this object.
     *
     * @return
     * @throws IOException
     */
    default InputStream[] getGzippedInputStreams() throws IOException {
        InputStream[] inputStreams = getInputStreams();
        if (inputStreams == null) {
            return null;
        }
        InputStream[] results = new InputStream[inputStreams.length];
        for (int i=0; i<inputStreams.length; i++) {
            InputStream is = inputStreams[i];
            results[i] = (is == null ? null : IOUtils.getGzippedInputStream(is));
        }
        return results;
    }

    /** Get the Gunzipped compressed data items for this object.
     *
     * @return
     * @throws IOException
     */
    default InputStream[] getGunzippedInputStreams() throws IOException {
        InputStream[] inputStreams = getInputStreams();
        if (inputStreams == null) {
            return null;
        }
        InputStream[] results = new InputStream[inputStreams.length];
        for (int i=0; i<inputStreams.length; i++) {
            InputStream is = inputStreams[i];
            results[i] = IOUtils.getGunzippedInputStream(is);
        }
        return results;
    }
}
