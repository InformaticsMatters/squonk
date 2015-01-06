package com.im.lac;

import java.util.Map;

/** Interface to allow a callback to collect results.
 * This class needs a better home, but right now its only used from the chemaxon-camel
 * module so it can stay there until its needed elsewhere.
 *
 * @author timbo
 */
public interface ResultExtractor<T> {
    
    public Map<String, Object> extractResults(T from);
    
}
