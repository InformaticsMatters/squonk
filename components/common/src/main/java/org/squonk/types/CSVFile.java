package org.squonk.types;

import org.squonk.util.CommonMimeTypes;

import java.io.InputStream;

/** Wrapper around data from an SD file to allow strong typing and type conversion
 *
 * @author timbo
 */
public class CSVFile extends AbstractFile {

    private static final String MEDIA_TYPE = CommonMimeTypes.MIME_TYPE_TEXT_CSV;

    public CSVFile(InputStream input) {
        super(input);
    }

    public String getMediaType() {
        return MEDIA_TYPE;
    }
    
}
