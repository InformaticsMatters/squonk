package org.squonk.options.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by timbo on 15/01/16.
 */
public class Structure implements Serializable {

    private final String source;
    private final String format;

    public Structure(@JsonProperty("source") String source, @JsonProperty("format") String format) {
        this.source = source;
        this.format = format;
    }

    public String getSource() {
        return source;
    }

    public String getFormat() {
        return format;
    }
}
