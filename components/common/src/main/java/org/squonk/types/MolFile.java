package org.squonk.types;

import org.squonk.util.CommonMimeTypes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/** Wrapper around data from a molfile to allow strong typing and type conversion
 *
 * @author timbo
 */
public class MolFile extends AbstractFile {

    private static final String MEDIA_TYPE = CommonMimeTypes.MIME_TYPE_MDL_MOLFILE;

    public MolFile(InputStream input) {
        super(input);
    }

    public MolFile(String input) {
        super(new ByteArrayInputStream(input.getBytes()));
    }

    public String getMediaType() {
        return MEDIA_TYPE;
    }
    
}
