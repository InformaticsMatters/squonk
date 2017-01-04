package org.squonk.util;

import java.io.*;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author timbo
 */
public class IOUtils {

    private static final Logger LOG = Logger.getLogger(IOUtils.class.getName());

    /**
     * Utility for checking if an InputStream is gzipped. Returns a
     * GZIPInputStream if the stream is in gzip format, otherwise the original
     * InputStream (actually a wrapper around it).
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
            LOG.fine("Stream is gzipped");
            return new GZIPInputStream(pb);
        } else {
            LOG.fine("Stream is not gzipped");
            return pb;
        }
    }

    public static InputStream getGzippedInputStream(InputStream is) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(is, 2);
        byte[] signature = new byte[2];
        pb.read(signature); //read the signature
        pb.unread(signature); //push back the signature to the stream
        if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b) {//check if matches standard gzip magic number
            LOG.fine("Stream is gzipped");
            return pb;
        } else {
            LOG.fine("Stream is not gzipped");

            final PipedInputStream pis = new PipedInputStream();
            final OutputStream out = new PipedOutputStream(pis);
            final OutputStream gzip = new GZIPOutputStream(out);

            final ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable c = (Callable) () -> {
                byte[] bytes = new byte[100];
                int len = 0;
                while ((len = pb.read(bytes)) > 0) {
                    gzip.write(bytes, 0, len);
                }
                pb.close();
                gzip.close();

                return true;
            };
            executor.submit(c);
            executor.shutdown();

            return pis;
        }
    }

    /**
     * Convenience method to close the object if it implements the Closeable
     * interface. If the close() fails then the exception is logged, but not
     * thrown.
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

    /**
     * Close and log any exception, but don't throw.
     *
     * @param o
     */
    public static void close(Closeable o) {
        try {
            o.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to close " + o, ex);
        }
    }

    /**
     * Convert with a default buffer size of 1000
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static String convertStreamToString(final InputStream is) throws IOException {
        return convertStreamToString(is, 1000);
    }

    public static String convertStreamToString(final InputStream is, final int bufferSize) throws IOException {
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        try (Reader in = new InputStreamReader(is)) {

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

    public static byte[] convertStreamToBytes(final InputStream is) throws IOException {
        return convertStreamToBytes(is, 1000);
    }

    public static byte[] convertStreamToBytes(final InputStream is, final int bufferSize) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while (true) {
                int rsz = is.read(buffer, 0, buffer.length);
                if (rsz < 0) {
                    break;
                }
                out.write(buffer, 0, rsz);
            }
            return out.toByteArray();
        }
    }

    public static long transfer(final InputStream in, final OutputStream out) throws IOException {
        return transfer(in, out, 1000);
    }

    /** Transfers the bytes in the input to the output. When finished the input is closed, but not the output as you might
     * need to write more to it.
     *
     * @param in
     * @param out
     * @param bufferSize
     * @return
     * @throws IOException
     */
    public static long transfer(final InputStream in, final OutputStream out, final int bufferSize) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        long count = 0;
        try  {

            while (true) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0) {
                    break;
                }
                out.write(buffer, 0, rsz);
                count += (long)rsz;
            }

        } finally {
            in.close();
        }
        return count;
    }

    public static String truncateString(String s, int maxLength) {
        if (s == null) {
            return null;
        }
        if (s.length() > maxLength) {
            return s.substring(0, maxLength - 4) + " ...";
        } else {
            return s;
        }
    }

    public static String joinArray(Object[] vals, String sep) {

        if (vals == null || vals.length == 0) {
            return "";
        }
        StringBuilder b = new StringBuilder(vals[0].toString());
        for (int i = 1; i < vals.length; i++) {
            b.append(sep).append(vals[i].toString());
        }
        return b.toString();
    }

    /**
     * Create an ordered Stream containing no null values from the Iterator
     *
     * @param <T>
     * @param iter
     * @param type
     * @return
     * @see {@link SimpleStreamProvider} for more control over
     * this.
     */
    public static <T> Stream<T> streamFromIterator(Iterator<T> iter, Class<T> type) {
        SimpleStreamProvider sp = new SimpleStreamProvider(iter, type);
        return sp.getStream();
    }

    /**
     * Create an ordered Stream containing no null values of the specified batch
     * size from the Iterator
     *
     * @param batchSize
     * @param <T>
     * @param iter
     * @param type
     * @return
     * @see {@link SimpleStreamProvider} for more control over
     * this.
     */
    public static <T> Stream<T> streamFromIterator(Iterator<T> iter, Class<T> type, int batchSize) {
        SimpleStreamProvider sp = new SimpleStreamProvider(iter, type, Spliterator.NONNULL | Spliterator.ORDERED, batchSize);
        return sp.getStream();
    }

    /**
     * Get a value that might be configured externally. Looks first for a system property
     * (a -D option specified to Java), if not present looks for an environment variable
     * and if not present then falls back to the specified default.
     *
     * @param name         The system property or environment variable name
     * @param defaultValue The value to fall back to.
     * @return
     */
    public static String getConfiguration(String name, String defaultValue) {
        String s = System.getProperty(name);
        if (s != null && s.length() > 0) {
            return s;
        }
        s = System.getenv(name);
        if (s != null && s.length() > 0) {
            return s;
        }
        return defaultValue;

    }


    public static String getDockerGateway() {
        String result = null;
        String dockerHost = IOUtils.getConfiguration("DOCKER_HOST", null);
        if (dockerHost != null && dockerHost.startsWith("tcp://")) {
            //tcp://192.168.99.100:2376
            String s = dockerHost.substring(6);
            int i = s.indexOf(":");
            if (i > 0) {
                result = s.substring(0, i);
            } else {
                result = s;
            }
        }
        String h = (result == null ? "localhost" : result);
        LOG.info("Docker Gateway: " + h);
        return h;
    }

}
