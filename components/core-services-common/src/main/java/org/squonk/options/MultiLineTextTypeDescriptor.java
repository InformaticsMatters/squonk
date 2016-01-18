package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by timbo on 15/01/16.
 */
@JsonIgnoreProperties({"type"})
public class MultiLineTextTypeDescriptor implements Serializable, TypeDescriptor<String> {

    public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    public static final String MIME_TYPE_TEXT_HTML = "text/html";
    public static final String MIME_TYPE_SCRIPT_GROOVY = "script/groovy";


    private final Integer rows;
    private final Integer cols;
    private final String mimeType;

    public MultiLineTextTypeDescriptor(
            @JsonProperty("rows") Integer rows,
            @JsonProperty("cols") Integer cols,
            @JsonProperty("mimeType") String mimeType) {
        this.rows = rows;
        this.cols = cols;
        this.mimeType = mimeType;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    public Integer getRows() {
        return rows;
    }

    public Integer getCols() {
        return cols;
    }

    public String getMimeType() {
        return mimeType;
    }
}
