package org.squonk.types;

import org.squonk.util.CommonMimeTypes;

import java.io.InputStream;

/** Wrapper around data from an SD file to allow strong typing and type conversion
 *
 * @author timbo
 */
public class SDFile extends AbstractStreamType {

    public static final String PROP_NAME_FIELD_NAME = "SDF_NAME_FIELD_NAME";
    private static final String MEDIA_TYPE = CommonMimeTypes.MIME_TYPE_MDL_SDF;
    
    public SDFile(InputStream input) {
        super(input);
    }

    public String getMediaType() {
        return MEDIA_TYPE;
    }
    
}
