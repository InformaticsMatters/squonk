package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by timbo on 15/01/16.
 */
@JsonIgnoreProperties({"type"})
public class MultiLineTextTypeDescriptor extends SimpleTypeDescriptor<String> {

    public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    public static final String MIME_TYPE_TEXT_HTML = "text/html";
    public static final String MIME_TYPE_SCRIPT_GROOVY = "text/x-groovy";
    public static final String MIME_TYPE_SCRIPT_SHELL = "text/x-shellscript";


    private final Integer rows;
    private final Integer cols;
    private final String mimeType;

    public MultiLineTextTypeDescriptor(
            @JsonProperty("rows") Integer rows,
            @JsonProperty("cols") Integer cols,
            @JsonProperty("mimeType") String mimeType) {
        super(String.class);
        this.rows = rows;
        this.cols = cols;
        this.mimeType = mimeType;
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
