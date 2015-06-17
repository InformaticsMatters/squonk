package com.im.lac.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author timbo
 */
public class IOUtils {

    private static final Logger LOG = Logger.getLogger(IOUtils.class.getName());

    /**
     * Utility for checking if an InputStream is gzipped. Returns a GZIPInputStream if the stream is
     * in gzip format, otherwise the original InputStream (actually a wrapper around it).
     *
     * @param is An InputStream that provides the data gunzipped.
     * @return
     * @throws IOException
     */
    public static InputStream getGunzippedInputStream(InputStream is) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(is, 2);
        byte[] signature = new byte[2];
        pb.read(signature); //read the signature
        pb.unread(signature); //push back the signature to the stream
        if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b) {//check if matches standard gzip magic number
            LOG.info("Stream is gzipped");
            return new GZIPInputStream(pb);
        } else {
            LOG.info("Stream is not gzipped");
            return pb;
        }
    }

    /**
     * Convenience method to close the object if it implements the Closeable interface. If the
     * close() fails then the exception is logged, but not thrown.
     *
     * @param o
     */
    public static void closeIfCloseable(Object o) {
        if (o instanceof Closeable) {
            try {
                ((Closeable) o).close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Failed to close " + o, ex);
            }
        }
    }

    public static String convertStreamToString(final InputStream is, final int bufferSize) throws IOException {
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is);

        while (true) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0) {
                break;
            }
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }

}
