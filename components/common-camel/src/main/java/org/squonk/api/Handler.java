package org.squonk.api;

/**
 * Created by timbo on 23/10/17.
 */
public interface Handler<T> {

    Class<T> getType();

    /** Whether streams should be gzipped as standard
     *
     * @return
     */
    default boolean isDefaultGzip() { return true; }

    default boolean shouldGzip(String key) {
        if (key == null) {
            return isDefaultGzip();
        } else {
            return key.toLowerCase().endsWith(".gz");
        }
    }
}
