package org.squonk.types;

import org.squonk.util.CommonMimeTypes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/** Wrapper around data from a PDB file to allow strong typing and type conversion
 *
 * @author timbo
 */
public class PDBFile extends AbstractFile {

    private static final String MEDIA_TYPE = CommonMimeTypes.MIME_TYPE_PDB;

    public PDBFile(InputStream input) {
        super(input);
    }

    public PDBFile(String input) {
        super(new ByteArrayInputStream(input.getBytes()));
    }

    public String getMediaType() {
        return MEDIA_TYPE;
    }
    
}
